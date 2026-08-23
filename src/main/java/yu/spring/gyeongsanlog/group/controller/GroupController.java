package yu.spring.gyeongsanlog.group.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yu.spring.gyeongsanlog.group.dto.CreateGroupRequest;
import yu.spring.gyeongsanlog.group.dto.GroupResponse;
import yu.spring.gyeongsanlog.group.service.GroupService;

@Tag(name = "group", description = "그룹 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group")
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "그룹 생성", description = "새 여행 그룹을 생성하고 생성자를 리더 겸 멤버로 등록한다.")
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(Authentication authentication, @Valid @RequestBody CreateGroupRequest request) {
        Long userId = Long.valueOf(authentication.getName());
        GroupResponse response = groupService.createGroup(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "초대코드로 그룹 참여", description = "초대코드에 해당하는 그룹에 멤버로 참여한다.")
    @PostMapping("/invite/{inviteCode}/join")
    public ResponseEntity<GroupResponse> joinGroup(Authentication authentication, @PathVariable String inviteCode) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(groupService.joinGroup(userId, inviteCode));
    }
}
