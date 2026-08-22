package yu.spring.gyeongsanlog.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yu.spring.gyeongsanlog.place.domain.PlaceImage;

import java.util.List;

public interface PlaceImageRepository extends JpaRepository<PlaceImage, Long> {

    List<PlaceImage> findAllByPlaceIdOrderBySortOrderAsc(Long placeId);

    @Query("SELECT DISTINCT pi.place.id FROM PlaceImage pi")
    List<Long> findAllPlaceIdsWithImages();

    @Modifying
    @Query("DELETE FROM PlaceImage pi WHERE pi.place.id = :placeId")
    void deleteAllByPlaceId(@Param("placeId") Long placeId);
}
