package yu.spring.gyeongsanlog.letter.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yu.spring.gyeongsanlog.letter.dto.LetterResponse;
import yu.spring.gyeongsanlog.letter.dto.WriteLetterRequest;
import yu.spring.gyeongsanlog.letter.service.LetterService;

@Tag(name = "letter", description = "편지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/log")
public class LetterController {

    private final LetterService letterService;

    @Operation(summary = "편지 남기기", description = "그룹 멤버에게 편지를 남긴다. 자기 자신에게는 남길 수 없고, 같은 사람에게는 그룹당 한 번만 가능하다.")
    @PostMapping("/{groupId}/letter")
    public ResponseEntity<LetterResponse> writeLetter(
            Authentication authentication,
            @PathVariable Long groupId,
            @Valid @RequestBody WriteLetterRequest request
    ) {
        Long userId = Long.valueOf(authentication.getName());
        LetterResponse response = letterService.writeLetter(userId, groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "편지 조회", description = "편지를 조회한다. 받는 사람만 조회할 수 있다.")
    @GetMapping("/{groupId}/{letterId}")
    public ResponseEntity<LetterResponse> getLetter(
            Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long letterId
    ) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(letterService.getLetter(userId, groupId, letterId));
    }
}
