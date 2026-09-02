package yu.spring.gyeongsanlog.place.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yu.spring.gyeongsanlog.place.domain.ContentType;
import yu.spring.gyeongsanlog.place.domain.Place;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByContentId(String contentId);

    boolean existsByContentId(String contentId);

    List<Place> findAllByContentIdIn(List<String> contentIds);

    // 종료된 축제는 둘러보기 목록에서 제외한다 (상세 조회는 그대로 열어둠)
    String ACTIVE_CONDITION =
            "(p.contentType <> 'FESTIVAL' OR p.eventEndDate IS NULL OR p.eventEndDate >= CURRENT_DATE)";

    // 무한스크롤이라 전체 개수가 필요 없어 Slice로 받는다
    @Query("SELECT p FROM Place p WHERE " + ACTIVE_CONDITION)
    Slice<Place> findAllBy(Pageable pageable);

    @Query("SELECT p FROM Place p WHERE p.contentType = :contentType AND " + ACTIVE_CONDITION)
    Slice<Place> findByContentType(@Param("contentType") ContentType contentType, Pageable pageable);

    @Query(value = "SELECT * FROM place WHERE content_type <> :excluded "
            + "AND (content_type <> 'FESTIVAL' OR event_end_date IS NULL OR event_end_date >= CURDATE()) "
            + "ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Place> findRandomExcluding(@Param("excluded") String excluded);

    // 장소 기준 추천용 랜덤 후보 (음식점·자기 자신·종료된 축제 제외)
    @Query(value = "SELECT * FROM place WHERE content_type <> :excluded AND id <> :excludeId "
            + "AND (content_type <> 'FESTIVAL' OR event_end_date IS NULL OR event_end_date >= CURDATE()) "
            + "ORDER BY RAND() LIMIT 5", nativeQuery = true)
    List<Place> findRandomCandidates(@Param("excluded") String excluded, @Param("excludeId") Long excludeId);

    // 카테고리 기준 추천용 랜덤 후보 (종료된 축제 제외)
    @Query(value = "SELECT * FROM place WHERE content_type = :contentType "
            + "AND (content_type <> 'FESTIVAL' OR event_end_date IS NULL OR event_end_date >= CURDATE()) "
            + "ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<Place> findRandomByContentType(@Param("contentType") String contentType);
}
