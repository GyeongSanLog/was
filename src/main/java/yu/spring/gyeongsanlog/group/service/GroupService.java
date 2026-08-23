package yu.spring.gyeongsanlog.group.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.exception.ErrorCode;
import yu.spring.gyeongsanlog.group.domain.GroupMember;
import yu.spring.gyeongsanlog.group.domain.TravelGroup;
import yu.spring.gyeongsanlog.group.dto.CreateGroupRequest;
import yu.spring.gyeongsanlog.group.dto.GroupDetailResponse;
import yu.spring.gyeongsanlog.group.dto.GroupMemberResponse;
import yu.spring.gyeongsanlog.group.dto.GroupResponse;
import yu.spring.gyeongsanlog.group.repository.GroupMemberRepository;
import yu.spring.gyeongsanlog.group.repository.TravelGroupRepository;
import yu.spring.gyeongsanlog.user.domain.User;
import yu.spring.gyeongsanlog.user.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TravelGroupRepository travelGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupResponse createGroup(Long userId, CreateGroupRequest request) {
        User leader = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        TravelGroup group = TravelGroup.builder()
                .name(request.getName())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .leader(leader)
                .inviteCode(generateUniqueInviteCode())
                .build();
        travelGroupRepository.save(group);

        groupMemberRepository.save(GroupMember.builder()
                .group(group)
                .user(leader)
                .joinedAt(LocalDateTime.now())
                .build());

        return GroupResponse.from(group);
    }

    @Transactional
    public void joinGroup(Long userId, String inviteCode) {
        TravelGroup group = travelGroupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        if (groupMemberRepository.existsByGroupIdAndUserId(group.getId(), userId)) {
            throw new BusinessException(ErrorCode.ALREADY_GROUP_MEMBER);
        }

        User user = userRepository.getReferenceById(userId);
        groupMemberRepository.save(GroupMember.builder()
                .group(group)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .build());
    }

    @Transactional(readOnly = true)
    public GroupDetailResponse getGroupDetail(Long userId, Long groupId) {
        TravelGroup group = travelGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        // 멤버가 아니면 그룹 존재 여부 자체를 숨기기 위해 404로 응답
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new BusinessException(ErrorCode.GROUP_NOT_FOUND);
        }

        List<GroupMemberResponse> members = groupMemberRepository.findByGroupIdWithUser(groupId).stream()
                .map(GroupMemberResponse::from)
                .toList();

        return GroupDetailResponse.of(group, members);
    }

    @Transactional
    public void withdrawGroup(Long userId, Long groupId) {
        TravelGroup group = travelGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new BusinessException(ErrorCode.GROUP_NOT_FOUND);
        }

        if (!group.getLeader().getId().equals(userId)) {
            groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
            return;
        }

        // 리더는 그룹에 혼자 남았을 때만 탈퇴 가능하며, 이 경우 그룹 자체를 삭제한다
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        if (members.size() > 1) {
            throw new BusinessException(ErrorCode.LEADER_CANNOT_LEAVE);
        }
        groupMemberRepository.deleteAll(members);
        travelGroupRepository.delete(group);
    }

    // 초대코드 중복은 사실상 발생하지 않지만(8자 36진수 = 약 2조 경우의 수) 유니크 제약 위반을 막기 위해 재시도
    private String generateUniqueInviteCode() {
        String code;
        do {
            code = generateInviteCode();
        } while (travelGroupRepository.findByInviteCode(code).isPresent());
        return code;
    }

    private String generateInviteCode() {
        StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            sb.append(INVITE_CODE_CHARS.charAt(RANDOM.nextInt(INVITE_CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
