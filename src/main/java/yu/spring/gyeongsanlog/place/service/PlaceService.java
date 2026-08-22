package yu.spring.gyeongsanlog.place.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yu.spring.gyeongsanlog.common.dto.SliceResponse;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.exception.ErrorCode;
import yu.spring.gyeongsanlog.place.domain.ContentType;
import yu.spring.gyeongsanlog.place.domain.Place;
import yu.spring.gyeongsanlog.place.dto.PlaceDetailResponse;
import yu.spring.gyeongsanlog.place.dto.PlaceListResponse;
import yu.spring.gyeongsanlog.place.repository.PlaceRepository;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;

    // 관광지 목록 조회 (유형 미지정 시 전체)
    @Transactional(readOnly = true)
    public SliceResponse<PlaceListResponse> getPlaces(ContentType contentType, Pageable pageable) {
        Slice<Place> places = (contentType == null)
                ? placeRepository.findAllBy(pageable)
                : placeRepository.findByContentType(contentType, pageable);

        return SliceResponse.from(places.map(PlaceListResponse::from));
    }

    // 관광지 상세 조회
    @Transactional(readOnly = true)
    public PlaceDetailResponse getPlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        return PlaceDetailResponse.from(place);
    }

    // 랜덤 관광지 조회 (음식점 제외)
    @Transactional(readOnly = true)
    public PlaceDetailResponse getRandomPlace() {
        Place place = placeRepository.findRandomExcluding(ContentType.RESTAURANT.name())
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        return PlaceDetailResponse.from(place);
    }
}
