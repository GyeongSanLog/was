package yu.spring.gyeongsanlog.group.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
}
