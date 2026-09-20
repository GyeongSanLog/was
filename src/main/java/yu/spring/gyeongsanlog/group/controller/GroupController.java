package yu.spring.gyeongsanlog.group.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import yu.spring.gyeongsanlog.group.service.GroupVideoMergeRetryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import yu.spring.gyeongsanlog.group.dto.CreateGroupRequest;
import yu.spring.gyeongsanlog.group.dto.GroupDetailResponse;
import yu.spring.gyeongsanlog.group.dto.GroupResponse;
import yu.spring.gyeongsanlog.group.service.GroupService;

import java.util.List;

@Tag(name = "group", description = "그룹 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group")
public class GroupController {

    private final GroupService groupService;
    private final GroupVideoMergeRetryService groupVideoMergeRetryService;

    @Operation(summary = "실패한 그룹 영상 병합 재시도",
            description = "종료된 여행의 FAILED 상태에서 그룹 멤버만 요청할 수 있다. 접수 후 백그라운드에서 병합하며 그룹 상세의 mergeStatus로 결과를 확인한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "재병합 요청 접수"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "404", description = "그룹이 없거나 그룹 멤버가 아님"),
            @ApiResponse(responseCode = "409", description = "여행이 종료되지 않았거나 FAILED 상태가 아님"),
            @ApiResponse(responseCode = "503", description = "병합 작업 대기열이 가득 참")
    })
    @PostMapping("/{groupId}/merge/retry")
    public ResponseEntity<Void> retryMerge(Authentication authentication, @PathVariable Long groupId) {
        Long userId = Long.valueOf(authentication.getName());
        groupVideoMergeRetryService.retry(userId, groupId);
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "그룹 생성", description = "새 여행 그룹을 생성하고 생성자를 리더 겸 멤버로 등록한다. 그룹 사진은 선택사항이다.")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<GroupResponse> createGroup(
            Authentication authentication,
            @Valid @RequestPart("request") CreateGroupRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        Long userId = Long.valueOf(authentication.getName());
        GroupResponse response = groupService.createGroup(userId, request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "내가 속한 그룹 목록 조회", description = "로그인한 사용자가 속한 그룹 목록을 최근 참여순으로 조회한다.")
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getMyGroups(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(groupService.getMyGroups(userId));
    }

    @Operation(summary = "초대코드로 그룹 참여", description = "초대코드에 해당하는 그룹에 멤버로 참여한다. 그룹 최대 인원(10명)을 넘으면 참여할 수 없다.")
    @PostMapping("/invite/{inviteCode}/join")
    public ResponseEntity<Void> joinGroup(Authentication authentication, @PathVariable String inviteCode) {
        Long userId = Long.valueOf(authentication.getName());
        groupService.joinGroup(userId, inviteCode);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "그룹 상세 조회", description = "그룹 정보와 멤버 목록을 조회한다. 그룹 멤버만 조회 가능하다.")
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailResponse> getGroupDetail(Authentication authentication, @PathVariable Long groupId) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(groupService.getGroupDetail(userId, groupId));
    }

    @Operation(summary = "그룹 탈퇴", description = "그룹에서 탈퇴한다. 리더는 그룹에 혼자 남았을 때만 탈퇴 가능하며 이 경우 그룹이 삭제된다.")
    @DeleteMapping("/{groupId}/withdraw")
    public ResponseEntity<Void> withdrawGroup(Authentication authentication, @PathVariable Long groupId) {
        Long userId = Long.valueOf(authentication.getName());
        groupService.withdrawGroup(userId, groupId);
        return ResponseEntity.noContent().build();
    }
}
