package yu.spring.gyeongsanlog.group.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import yu.spring.gyeongsanlog.clip.domain.Clip;
import yu.spring.gyeongsanlog.clip.repository.ClipRepository;
import yu.spring.gyeongsanlog.common.util.S3Uploader;
import yu.spring.gyeongsanlog.group.domain.TravelGroup;
import yu.spring.gyeongsanlog.group.repository.TravelGroupRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// 그룹의 클립들을 촬영순으로 이어붙이고 코멘트를 자막처럼 입혀 하나의 영상으로 병합한다.
// 아무도 찍지 않은 시간대는 검정 화면 + "놓친 순간" 자막으로 채우고,
// 같은 시간대에 여러 명이 찍었으면 한 화면에 동시에(2명은 상하 분할, 3명 이상은 2열 그리드) 보여준다.
// ffmpeg 프로세스 실행이 오래 걸릴 수 있어 DB 트랜잭션 밖에서 처리하고, 상태 갱신만 별도 빈(GroupMergeStatusUpdater)에 위임한다.
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupVideoMergeService {

    private static final int TARGET_WIDTH = 720;
    private static final int TARGET_HEIGHT = 1280;
    private static final int PLACEHOLDER_DURATION_SECONDS = 2;
    private static final int FILLER_CELL_DURATION_SECONDS = 5; // 실제 클립(약 2초)보다 넉넉히 길게 잡아 그리드 스택이 잘리지 않게 함
    private static final String MISSED_SLOT_TEXT = "놓친 순간";

    private final ClipRepository clipRepository;
    private final TravelGroupRepository travelGroupRepository;
    private final S3Uploader s3Uploader;
    private final GroupMergeStatusUpdater statusUpdater;

    @Value("${merge.font-path}")
    private String fontPath;

    private record DownloadedClip(Path videoPath, String comment, String nickname) {}

    public void mergeGroupVideo(Long groupId) {
        statusUpdater.markProcessing(groupId);

        Path workDir = null;
        try {
            TravelGroup group = travelGroupRepository.findById(groupId)
                    .orElseThrow(() -> new IllegalStateException("그룹을 찾을 수 없습니다. groupId: " + groupId));
            // 닉네임을 자막에 넣어야 해서 user까지 fetch join(트랜잭션 밖이라 lazy loading 불가)
            List<Clip> clips = clipRepository.findByGroupIdWithUser(groupId);
            if (clips.isEmpty()) {
                throw new IllegalStateException("병합할 클립이 없습니다. groupId: " + groupId);
            }

            int totalSlots = Math.max(1, (int) Math.ceil(
                    Duration.between(group.getStartAt(), group.getEndAt()).toMinutes() / 60.0));
            Map<Integer, List<Clip>> clipsBySlot = clips.stream()
                    .collect(Collectors.groupingBy(Clip::getSlotIndex, TreeMap::new, Collectors.toList()));

            workDir = Files.createTempDirectory("clip-merge-" + groupId + "-");
            List<List<DownloadedClip>> slots = downloadBySlot(clipsBySlot, totalSlots, workDir);
            Path output = workDir.resolve("merged.mp4");
            runFfmpeg(slots, output);

            String key = "MERGED_VIDEO/" + groupId + "_" + UUID.randomUUID() + ".mp4";
            s3Uploader.uploadFile(key, output);
            String url = s3Uploader.getPublicUrl(key);

            statusUpdater.markDone(groupId, url);
        } catch (Exception e) {
            log.error("그룹 영상 병합 실패. groupId: {}", groupId, e);
            statusUpdater.markFailed(groupId);
        } finally {
            if (workDir != null) {
                deleteRecursively(workDir);
            }
        }
    }

    // 슬롯(0..totalSlots-1) 순서로 그 슬롯에 있는 클립들을 전부 다운로드한다. 빈 슬롯은 빈 리스트.
    private List<List<DownloadedClip>> downloadBySlot(Map<Integer, List<Clip>> clipsBySlot, int totalSlots, Path workDir)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        List<List<DownloadedClip>> slots = new ArrayList<>();
        int fileIndex = 0;

        for (int slot = 0; slot < totalSlots; slot++) {
            List<Clip> slotClips = clipsBySlot.get(slot);
            if (slotClips == null || slotClips.isEmpty()) {
                slots.add(List.of());
                continue;
            }
            List<DownloadedClip> downloaded = new ArrayList<>();
            for (Clip clip : slotClips) {
                Path target = workDir.resolve("clip" + fileIndex++ + ".mp4");
                HttpRequest request = HttpRequest.newBuilder(URI.create(clip.getVideoUrl())).GET().build();
                HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(target));
                if (response.statusCode() != 200) {
                    throw new IOException("클립 다운로드 실패(HTTP " + response.statusCode() + "): " + clip.getVideoUrl());
                }
                downloaded.add(new DownloadedClip(target, clip.getComment(), clip.getUser().getNickname()));
            }
            slots.add(downloaded);
        }
        return slots;
    }

    private void runFfmpeg(List<List<DownloadedClip>> slots, Path output) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");

        StringBuilder filter = new StringBuilder();
        List<String> slotVideoLabels = new ArrayList<>();
        List<String> slotAudioLabels = new ArrayList<>();
        int[] inputIndex = {0};

        for (int slot = 0; slot < slots.size(); slot++) {
            List<DownloadedClip> clips = slots.get(slot);
            if (clips.isEmpty()) {
                addPlaceholderSlot(command, filter, inputIndex, slot, slotVideoLabels, slotAudioLabels);
            } else if (clips.size() == 1) {
                addSingleClipSlot(command, filter, inputIndex, slot, clips.get(0), slotVideoLabels, slotAudioLabels);
            } else {
                addGridSlot(command, filter, inputIndex, slot, clips, slotVideoLabels, slotAudioLabels);
            }
        }

        StringBuilder concatInputs = new StringBuilder();
        for (int i = 0; i < slotVideoLabels.size(); i++) {
            concatInputs.append(slotVideoLabels.get(i)).append(slotAudioLabels.get(i));
        }
        filter.append(concatInputs).append("concat=n=").append(slots.size()).append(":v=1:a=1[outv][outa]");

        command.add("-filter_complex");
        command.add(filter.toString());
        command.add("-map");
        command.add("[outv]");
        command.add("-map");
        command.add("[outa]");
        command.add("-c:v");
        command.add("libx264");
        command.add("-preset");
        command.add("veryfast");
        command.add("-c:a");
        command.add("aac");
        command.add("-movflags");
        command.add("+faststart");
        command.add(output.toString());

        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String ffmpegOutput = new String(process.getInputStream().readAllBytes());
        boolean finished = process.waitFor(5, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("ffmpeg 처리 시간 초과");
        }
        if (process.exitValue() != 0) {
            log.error("ffmpeg 실패 출력:\n{}", ffmpegOutput);
            throw new IOException("ffmpeg 종료 코드 " + process.exitValue());
        }
    }

    private void addPlaceholderSlot(List<String> command, StringBuilder filter, int[] inputIndex, int slot,
                                     List<String> slotVideoLabels, List<String> slotAudioLabels) {
        command.add("-f");
        command.add("lavfi");
        command.add("-t");
        command.add(String.valueOf(PLACEHOLDER_DURATION_SECONDS));
        command.add("-i");
        command.add("color=c=black:s=" + TARGET_WIDTH + "x" + TARGET_HEIGHT + ":r=25");
        int videoIdx = inputIndex[0]++;

        command.add("-f");
        command.add("lavfi");
        command.add("-t");
        command.add(String.valueOf(PLACEHOLDER_DURATION_SECONDS));
        command.add("-i");
        command.add("anullsrc=r=44100:cl=stereo");
        int audioIdx = inputIndex[0]++;

        String label = "slot" + slot + "v";
        filter.append(buildCellFilter(videoIdx, TARGET_WIDTH, TARGET_HEIGHT, MISSED_SLOT_TEXT, null))
                .append('[').append(label).append("];");
        slotVideoLabels.add('[' + label + ']');
        slotAudioLabels.add("[" + audioIdx + ":a]");
    }

    private void addSingleClipSlot(List<String> command, StringBuilder filter, int[] inputIndex, int slot,
                                    DownloadedClip clip, List<String> slotVideoLabels, List<String> slotAudioLabels) {
        command.add("-i");
        command.add(clip.videoPath().toString());
        int idx = inputIndex[0]++;

        String label = "slot" + slot + "v";
        filter.append(buildCellFilter(idx, TARGET_WIDTH, TARGET_HEIGHT, clip.comment(), clip.nickname()))
                .append('[').append(label).append("];");
        slotVideoLabels.add('[' + label + ']');
        slotAudioLabels.add("[" + idx + ":a]");
    }

    // 같은 시간대에 여러 명이 찍었으면 한 화면에 동시에 보여준다: 2명은 상하 분할(1열), 3명 이상은 2열 그리드.
    // 빈 칸이 남으면(홀수 인원) 자막 없는 검정 셀로 채운다.
    private void addGridSlot(List<String> command, StringBuilder filter, int[] inputIndex, int slot,
                              List<DownloadedClip> clips, List<String> slotVideoLabels, List<String> slotAudioLabels) {
        int n = clips.size();
        int cols = (n == 2) ? 1 : 2;
        int rows = (n == 2) ? 2 : (int) Math.ceil(n / 2.0);
        int cellW = TARGET_WIDTH / cols;
        int cellH = TARGET_HEIGHT / rows;
        int totalCells = rows * cols;

        List<String> cellLabels = new ArrayList<>();
        List<Integer> realAudioIdx = new ArrayList<>();

        for (int cell = 0; cell < totalCells; cell++) {
            String cellLabel = "slot" + slot + "c" + cell;
            if (cell < n) {
                DownloadedClip clip = clips.get(cell);
                command.add("-i");
                command.add(clip.videoPath().toString());
                int idx = inputIndex[0]++;
                realAudioIdx.add(idx);
                filter.append(buildCellFilter(idx, cellW, cellH, clip.comment(), clip.nickname()))
                        .append('[').append(cellLabel).append("];");
            } else {
                command.add("-f");
                command.add("lavfi");
                command.add("-t");
                command.add(String.valueOf(FILLER_CELL_DURATION_SECONDS));
                command.add("-i");
                command.add("color=c=black:s=" + cellW + "x" + cellH + ":r=25");
                int idx = inputIndex[0]++;
                filter.append('[').append(idx).append(":v]setsar=1[").append(cellLabel).append("];");
            }
            cellLabels.add(cellLabel);
        }

        List<String> rowLabels = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            if (cols == 1) {
                rowLabels.add(cellLabels.get(r));
            } else {
                String rowLabel = "slot" + slot + "row" + r;
                filter.append('[').append(cellLabels.get(r * cols)).append(']')
                        .append('[').append(cellLabels.get(r * cols + 1)).append(']')
                        .append("hstack=inputs=2:shortest=1[").append(rowLabel).append("];");
                rowLabels.add(rowLabel);
            }
        }

        String videoLabel = "slot" + slot + "v";
        StringBuilder vstackInputs = new StringBuilder();
        for (String rowLabel : rowLabels) {
            vstackInputs.append('[').append(rowLabel).append(']');
        }
        filter.append(vstackInputs).append("vstack=inputs=").append(rows).append(":shortest=1[").append(videoLabel).append("];");
        slotVideoLabels.add('[' + videoLabel + ']');

        String audioLabel = "slot" + slot + "a";
        StringBuilder amixInputs = new StringBuilder();
        for (int idx : realAudioIdx) {
            amixInputs.append('[').append(idx).append(":a]");
        }
        filter.append(amixInputs).append("amix=inputs=").append(realAudioIdx.size())
                .append(":duration=first[").append(audioLabel).append("];");
        slotAudioLabels.add('[' + audioLabel + ']');
    }

    // 셀 하나의 영상 필터: 크기 맞추기(scale+pad) + 가운데 코멘트 자막 + 좌상단 닉네임. 셀 크기에 맞춰 폰트 크기도 비례해서 줄인다.
    private String buildCellFilter(int inputIdx, int cellWidth, int cellHeight, String comment, String nickname) {
        int commentFontSize = clamp(cellWidth / 15, 20, 42);
        int nicknameFontSize = clamp(cellWidth / 22, 14, 32);

        StringBuilder sb = new StringBuilder();
        sb.append('[').append(inputIdx).append(":v]")
                .append("scale=").append(cellWidth).append(':').append(cellHeight)
                .append(":force_original_aspect_ratio=decrease,")
                .append("pad=").append(cellWidth).append(':').append(cellHeight).append(":(ow-iw)/2:(oh-ih)/2,")
                .append("setsar=1");

        if (comment != null && !comment.isBlank()) {
            sb.append(",drawtext=fontfile=").append(quote(fontPath))
                    .append(":text=").append(quote(comment))
                    .append(":fontcolor=white:fontsize=").append(commentFontSize)
                    .append(":x=(w-text_w)/2:y=(h-text_h)/2");
        }
        if (nickname != null && !nickname.isBlank()) {
            sb.append(",drawtext=fontfile=").append(quote(fontPath))
                    .append(":text=").append(quote(nickname))
                    .append(":fontcolor=white:fontsize=").append(nicknameFontSize)
                    .append(":x=16:y=16");
        }
        return sb.toString();
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    // ffmpeg 필터 옵션 값 이스케이핑. 콜론은 \: 로 escape(Windows 드라이브 콜론 포함해서 통과 확인함).
    // 작은따옴표(')는 \', '\'' 방식 둘 다 이 ffmpeg 빌드에서 파싱이 깨지거나 문자가 누락돼서,
    // 자막에 굽는 텍스트에서만 시각적으로 비슷한 유니코드 따옴표(’)로 바꿔 문제를 원천 차단한다. 저장되는 comment 원본은 그대로 둔다.
    private String quote(String value) {
        String sanitized = value.replace("'", "’");
        String escaped = sanitized.replace("\\", "\\\\").replace(":", "\\:");
        return "'" + escaped + "'";
    }

    private void deleteRecursively(Path dir) {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException e) {
            log.warn("임시 디렉터리 정리 실패: {}", dir, e);
        }
    }
}
