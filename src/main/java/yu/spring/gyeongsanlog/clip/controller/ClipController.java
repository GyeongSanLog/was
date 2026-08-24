package yu.spring.gyeongsanlog.clip.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import yu.spring.gyeongsanlog.clip.dto.ClipResponse;
import yu.spring.gyeongsanlog.clip.dto.UploadClipRequest;
import yu.spring.gyeongsanlog.clip.service.ClipService;

import java.util.List;

@Tag(name = "clip", description = "클립(영상) 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/log")
public class ClipController {

    private final ClipService clipService;

    @Operation(summary = "클립 업로드", description = "그룹 멤버가 촬영한 영상을 업로드한다. 촬영 시각 기준으로 시간대(slotIndex)가 자동 계산되며, 같은 시간대에 중복 업로드할 수 없다.")
    @PostMapping(value = "/{groupId}", consumes = "multipart/form-data")
    public ResponseEntity<ClipResponse> uploadClip(
            Authentication authentication,
            @PathVariable Long groupId,
            @Valid @RequestPart("request") UploadClipRequest request,
            @RequestPart("file") MultipartFile file
    ) {
        Long userId = Long.valueOf(authentication.getName());
        ClipResponse response = clipService.uploadClip(userId, groupId, request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "그룹 클립 피드 조회", description = "그룹의 클립을 촬영 시각순으로 조회한다. 그룹 멤버만 조회 가능하다.")
    @GetMapping("/{groupId}")
    public ResponseEntity<List<ClipResponse>> getClipFeed(Authentication authentication, @PathVariable Long groupId) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(clipService.getClipFeed(userId, groupId));
    }
}
