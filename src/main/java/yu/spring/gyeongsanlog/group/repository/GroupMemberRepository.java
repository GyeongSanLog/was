package yu.spring.gyeongsanlog.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yu.spring.gyeongsanlog.group.domain.GroupMember;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupId(Long groupId);

    List<GroupMember> findByUserId(Long userId);
}
