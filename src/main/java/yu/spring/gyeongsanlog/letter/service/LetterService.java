package yu.spring.gyeongsanlog.letter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.exception.ErrorCode;
import yu.spring.gyeongsanlog.group.domain.TravelGroup;
import yu.spring.gyeongsanlog.group.repository.GroupMemberRepository;
import yu.spring.gyeongsanlog.group.repository.TravelGroupRepository;
import yu.spring.gyeongsanlog.letter.domain.Letter;
import yu.spring.gyeongsanlog.letter.dto.LetterResponse;
import yu.spring.gyeongsanlog.letter.dto.WriteLetterRequest;
import yu.spring.gyeongsanlog.letter.repository.LetterRepository;
import yu.spring.gyeongsanlog.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final TravelGroupRepository travelGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public LetterResponse writeLetter(Long senderId, Long groupId, WriteLetterRequest request) {
        if (senderId.equals(request.getReceiverId())) {
            throw new BusinessException(ErrorCode.SELF_LETTER_NOT_ALLOWED);
        }

        TravelGroup group = travelGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        // 비멤버가 요청했으면 그룹 존재 여부 자체를 숨기기 위해 404로 응답
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, senderId)) {
            throw new BusinessException(ErrorCode.GROUP_NOT_FOUND);
        }
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, request.getReceiverId())) {
            throw new BusinessException(ErrorCode.RECEIVER_NOT_GROUP_MEMBER);
        }
        if (letterRepository.existsByGroupIdAndSenderIdAndReceiverId(groupId, senderId, request.getReceiverId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LETTER);
        }

        Letter letter = Letter.builder()
                .group(group)
                .sender(userRepository.getReferenceById(senderId))
                .receiver(userRepository.getReferenceById(request.getReceiverId()))
                .content(request.getContent())
                .build();
        letterRepository.save(letter);

        return LetterResponse.from(letter);
    }

    // 받는 사람이 아니면 편지 존재 여부 자체를 숨기기 위해 404로 응답
    @Transactional(readOnly = true)
    public LetterResponse getLetter(Long userId, Long groupId, Long letterId) {
        Letter letter = letterRepository.findByIdAndGroupIdWithSender(letterId, groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LETTER_NOT_FOUND));

        if (!letter.getReceiver().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.LETTER_NOT_FOUND);
        }

        return LetterResponse.from(letter);
    }
}
