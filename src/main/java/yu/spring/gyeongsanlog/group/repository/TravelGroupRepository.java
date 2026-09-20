package yu.spring.gyeongsanlog.group.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yu.spring.gyeongsanlog.group.domain.MergeStatus;
import yu.spring.gyeongsanlog.group.domain.TravelGroup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TravelGroupRepository extends JpaRepository<TravelGroup, Long> {

    Optional<TravelGroup> findByInviteCode(String inviteCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM TravelGroup g WHERE g.id = :groupId")
    Optional<TravelGroup> findByIdForUpdate(@Param("groupId") Long groupId);

    List<TravelGroup> findByEndAtBeforeAndMergeStatus(LocalDateTime endAt, MergeStatus mergeStatus);

    // 지금 여행이 진행 중인 그룹 (알림 발송 대상)
    List<TravelGroup> findByStartAtBeforeAndEndAtAfter(LocalDateTime start, LocalDateTime end);
}
