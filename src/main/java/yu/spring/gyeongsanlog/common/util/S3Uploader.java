package yu.spring.gyeongsanlog.common.util;

import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import yu.spring.gyeongsanlog.common.domain.FileType;
import yu.spring.gyeongsanlog.common.dto.FileDetailDto;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.exception.ErrorCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Uploader {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxVideoSize;

    private static final DataSize MAX_IMAGE_SIZE = DataSize.ofMegabytes(50);

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic"
    );

    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "mp4", "mov", "avi", "mkv", "webm"
    );

    public FileType getFileType(String fileName) {
        String extension = getExtension(fileName);
        if (IMAGE_EXTENSIONS.contains(extension)) {
            return FileType.IMAGE;
        } else if (VIDEO_EXTENSIONS.contains(extension)) {
            return FileType.VIDEO;
        } else {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    public FileDetailDto makeMetaData(MultipartFile file, String dirName) {
        validateFile(file);
        String originalFilename = file.getOriginalFilename();

        String extension = getExtension(originalFilename);
        String baseName = getBaseName(originalFilename).replaceAll("[^a-zA-Z0-9._-]", "_");

        String key = dirName + "/" + UUID.randomUUID() + "_" + baseName
                + (extension.isEmpty() ? "" : "." + extension);

        return FileDetailDto.builder()
                .originalFileName(originalFilename)
                .key(key)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .fileType(getFileType(originalFilename))
                .build();
    }

    public void uploadFile(String uploadKey, MultipartFile file) {
        validateFile(file);
        fileUpload(file, uploadKey);
    }

    // ffmpeg 결과물 등 서버가 로컬에 만든 파일을 업로드할 때 사용 (MultipartFile 기반 검증 대상이 아님)
    public void uploadFile(String uploadKey, Path filePath) {
        try {
            s3Template.upload(bucket, uploadKey, Files.newInputStream(filePath));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        } catch (S3Exception e) {
            log.error("S3 파일 업로드 실패. key: {}", uploadKey, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public String getSignedUrl(String key) {
        validateKey(key);
        try {
            return s3Template.createSignedGetURL(bucket, key, Duration.ofMinutes(10)).toString();
        } catch (S3Exception e) {
            log.error("S3 파일 URL 발급 실패. key: {}", key, e);
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    // 프로필 사진 등 항상 노출되는 퍼블릭 URL
    public String getPublicUrl(String key) {
        validateKey(key);
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }

    public void deleteFile(String key) {
        validateKey(key);
        try {
            s3Template.deleteObject(bucket, key);
        } catch (S3Exception e) {
            log.error("S3 파일 삭제 실패. key: {}", key, e);
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    private void validateKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException(ErrorCode.FILENAME_MISSING);
        }

        String extension = getExtension(originalFilename);
        boolean isImage = IMAGE_EXTENSIONS.contains(extension);
        boolean isVideo = VIDEO_EXTENSIONS.contains(extension);
        if (!isImage && !isVideo) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }

        long fileSize = file.getSize();
        if (isImage && fileSize > MAX_IMAGE_SIZE.toBytes()) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        if (isVideo && fileSize > maxVideoSize.toBytes()) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    private String getBaseName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "";
        }
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex == -1) return originalFilename;
        return originalFilename.substring(0, lastDotIndex);
    }

    private void fileUpload(MultipartFile file, String key) {
        try {
            s3Template.upload(bucket, key, file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        } catch (S3Exception e) {
            log.error("S3 파일 업로드 실패. key: {}", key, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
