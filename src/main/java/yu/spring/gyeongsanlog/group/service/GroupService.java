package yu.spring.gyeongsanlog.group.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.exception.ErrorCode;
import yu.spring.gyeongsanlog.group.domain.GroupMember;
import yu.spring.gyeongsanlog.group.domain.TravelGroup;
import yu.spring.gyeongsanlog.group.dto.CreateGroupRequest;
import yu.spring.gyeongsanlog.group.dto.GroupResponse;
import yu.spring.gyeongsanlog.group.repository.GroupMemberRepository;
import yu.spring.gyeongsanlog.group.repository.TravelGroupRepository;
import yu.spring.gyeongsanlog.user.domain.User;
import yu.spring.gyeongsanlog.user.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

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
