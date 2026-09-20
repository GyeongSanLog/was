package yu.spring.gyeongsanlog.place.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import yu.spring.gyeongsanlog.place.config.dto.DetailImageItem;
import yu.spring.gyeongsanlog.place.domain.Place;
import yu.spring.gyeongsanlog.place.domain.PlaceImage;
import yu.spring.gyeongsanlog.place.repository.PlaceImageRepository;
import yu.spring.gyeongsanlog.place.repository.PlaceRepository;

import java.util.ArrayList;
import java.util.List;

/*
 PlaceSyncService에서 TourAPI를 호출하는 도중 이 서비스를 호출하는데,
 별도 빈으로 분리해야 @Transactional이 프록시를 거쳐 실제로 적용됨
 */
@Service
@RequiredArgsConstructor
public class PlaceImageSyncService {

    private static final int MAX_IMAGES = 8;

    private final PlaceRepository placeRepository;
    private final PlaceImageRepository placeImageRepository;

    @Transactional
    public int replaceImages(Long placeId, List<DetailImageItem> items) {
        placeImageRepository.deleteAllByPlaceId(placeId);

        List<DetailImageItem> valid = items.stream()
                .filter(item -> StringUtils.hasText(item.getOriginimgurl()))
                .limit(MAX_IMAGES)
                .toList();

        if (valid.isEmpty()) {
            return 0;
        }

        Place placeRef = placeRepository.getReferenceById(placeId);

        List<PlaceImage> images = new ArrayList<>();
        for (int i = 0; i < valid.size(); i++) {
            DetailImageItem item = valid.get(i);
            images.add(PlaceImage.builder()
                    .place(placeRef)
                    .originUrl(item.getOriginimgurl())
                    .smallUrl(item.getSmallimageurl())
                    .sortOrder(i)
                    .build());
        }

        placeImageRepository.saveAll(images);
        return images.size();
    }
}
