package yu.spring.gyeongsanlog.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yu.spring.gyeongsanlog.group.domain.GroupMember;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupId(Long groupId);

    List<GroupMember> findByUserId(Long userId);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    void deleteByGroupIdAndUserId(Long groupId, Long userId);

    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.user WHERE gm.group.id = :groupId ORDER BY gm.joinedAt ASC")
    List<GroupMember> findByGroupIdWithUser(@Param("groupId") Long groupId);

    // 최근 참여한 그룹이 먼저 보이도록 구현
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.group g JOIN FETCH g.leader WHERE gm.user.id = :userId ORDER BY gm.joinedAt DESC")
    List<GroupMember> findByUserIdWithGroup(@Param("userId") Long userId);
}
