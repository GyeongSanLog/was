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

    // 무한스크롤이라 전체 개수가 필요 없어 Slice로 받는다
    Slice<Place> findAllBy(Pageable pageable);

    Slice<Place> findByContentType(ContentType contentType, Pageable pageable);

    @Query(value = "SELECT * FROM place WHERE content_type <> :excluded ORDER BY RAND() LIMIT 1",
            nativeQuery = true)
    Optional<Place> findRandomExcluding(@Param("excluded") String excluded);

    // 장소 기준 추천용 랜덤 후보 (음식점·자기 자신 제외)
    @Query(value = "SELECT * FROM place WHERE content_type <> :excluded AND id <> :excludeId ORDER BY RAND() LIMIT 5",
            nativeQuery = true)
    List<Place> findRandomCandidates(@Param("excluded") String excluded, @Param("excludeId") Long excludeId);

    // 카테고리 기준 추천용 랜덤 후보
    @Query(value = "SELECT * FROM place WHERE content_type = :contentType ORDER BY RAND() LIMIT 3",
            nativeQuery = true)
    List<Place> findRandomByContentType(@Param("contentType") String contentType);
}
