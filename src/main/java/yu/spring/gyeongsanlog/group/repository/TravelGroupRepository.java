package yu.spring.gyeongsanlog.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yu.spring.gyeongsanlog.group.domain.TravelGroup;

import java.util.Optional;

public interface TravelGroupRepository extends JpaRepository<TravelGroup, Long> {

    Optional<TravelGroup> findByInviteCode(String inviteCode);
}
