package yu.spring.gyeongsanlog.letter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yu.spring.gyeongsanlog.letter.domain.Letter;

import java.util.Optional;

public interface LetterRepository extends JpaRepository<Letter, Long> {

    boolean existsByGroupIdAndSenderIdAndReceiverId(Long groupId, Long senderId, Long receiverId);

    @Query("SELECT l FROM Letter l JOIN FETCH l.sender WHERE l.id = :letterId AND l.group.id = :groupId")
    Optional<Letter> findByIdAndGroupIdWithSender(@Param("letterId") Long letterId, @Param("groupId") Long groupId);
}
