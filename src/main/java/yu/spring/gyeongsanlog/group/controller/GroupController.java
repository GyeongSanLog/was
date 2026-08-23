package yu.spring.gyeongsanlog.group.controller;

import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "group", description = "그룹 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group")
public class GroupController {

    private final GroupService groupService;

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
    
    @Operation(summary = "초대코드로 그룹 참여", description = "초대코드에 해당하는 그룹에 멤버로 참여한다.")
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
