package yu.spring.gyeongsanlog.clip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yu.spring.gyeongsanlog.clip.domain.Clip;

import java.util.List;

public interface ClipRepository extends JpaRepository<Clip, Long> {

    List<Clip> findByGroupIdOrderByCapturedAtAsc(Long groupId);

    List<Clip> findByGroupIdAndUserId(Long groupId, Long userId);

    boolean existsByGroupIdAndUserIdAndSlotIndex(Long groupId, Long userId, Integer slotIndex);
}
