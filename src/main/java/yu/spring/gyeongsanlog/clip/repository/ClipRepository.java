package yu.spring.gyeongsanlog.clip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yu.spring.gyeongsanlog.clip.domain.Clip;

import java.util.List;

public interface ClipRepository extends JpaRepository<Clip, Long> {

    List<Clip> findByGroupIdOrderByCapturedAtAsc(Long groupId);

    List<Clip> findByGroupIdAndUserId(Long groupId, Long userId);

    boolean existsByGroupIdAndUserIdAndSlotIndex(Long groupId, Long userId, Integer slotIndex);

    @Query("SELECT c FROM Clip c JOIN FETCH c.user WHERE c.group.id = :groupId ORDER BY c.capturedAt ASC")
    List<Clip> findByGroupIdWithUser(@Param("groupId") Long groupId);

    void deleteByGroupId(Long groupId);
}
