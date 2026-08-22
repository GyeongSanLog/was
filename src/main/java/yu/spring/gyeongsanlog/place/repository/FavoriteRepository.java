package yu.spring.gyeongsanlog.place.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yu.spring.gyeongsanlog.place.domain.Favorite;
import yu.spring.gyeongsanlog.place.domain.Place;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);

    long deleteByUserIdAndPlaceId(Long userId, Long placeId);

    // 최근 찜한 순으로 반환
    @Query("SELECT f.place FROM Favorite f WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    Slice<Place> findFavoritePlacesByUserId(@Param("userId") Long userId, Pageable pageable);
}
