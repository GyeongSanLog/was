package yu.spring.gyeongsanlog.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yu.spring.gyeongsanlog.place.domain.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);

    long deleteByUserIdAndPlaceId(Long userId, Long placeId);
}
