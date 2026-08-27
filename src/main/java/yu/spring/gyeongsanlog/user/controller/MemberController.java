package yu.spring.gyeongsanlog.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import yu.spring.gyeongsanlog.common.exception.ErrorResponse;
import yu.spring.gyeongsanlog.user.dto.ChangePasswordRequest;
import yu.spring.gyeongsanlog.user.dto.FcmTokenRequest;
import yu.spring.gyeongsanlog.user.dto.MemberProfileResponse;
import yu.spring.gyeongsanlog.user.dto.UpdateProfileRequest;
import yu.spring.gyeongsanlog.user.service.MemberService;

@Tag(name = "member", description = "마이페이지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member/me")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 정보 조회", description = "로그인한 본인의 회원 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberProfileResponse.class)))
    })
    @GetMapping
    public ResponseEntity<MemberProfileResponse> getProfile(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(memberService.getProfile(userId));
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인 후 새 비밀번호로 변경한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "변경 성공"),
            @ApiResponse(responseCode = "401", description = "현재 비밀번호 불일치",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = Long.valueOf(authentication.getName());
        memberService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "FCM 토큰 등록", description = "푸시 알림을 받을 기기 토큰을 등록/갱신한다. 로그인 후와 토큰 갱신 시 호출한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "등록 성공")
    })
    @PatchMapping("/fcm-token")
    public ResponseEntity<Void> updateFcmToken(Authentication authentication, @Valid @RequestBody FcmTokenRequest request) {
        Long userId = Long.valueOf(authentication.getName());
        memberService.updateFcmToken(userId, request.getFcmToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "회원 정보 수정", description = "닉네임과 프로필 사진을 수정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = MemberProfileResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 사용중인 닉네임",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping(value = "", consumes = "multipart/form-data")
    public ResponseEntity<MemberProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestPart("request") UpdateProfileRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(memberService.updateProfile(userId, request, profileImage));
    }
}
