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
import yu.spring.gyeongsanlog.place.repository.PlaceImageRepository;
import yu.spring.gyeongsanlog.place.repository.PlaceRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private static final double EARTH_RADIUS_METERS = 6_371_000;
    private static final int RECOMMENDATION_SIZE = 3;

    private final PlaceRepository placeRepository;
    private final PlaceImageRepository placeImageRepository;

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

        return PlaceDetailResponse.from(place, placeImageRepository.findAllByPlaceIdOrderBySortOrderAsc(placeId));
    }

    // 랜덤 관광지 조회 (음식점 제외)
    @Transactional(readOnly = true)
    public PlaceDetailResponse getRandomPlace() {
        Place place = placeRepository.findRandomExcluding(ContentType.RESTAURANT.name())
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        return PlaceDetailResponse.from(place, placeImageRepository.findAllByPlaceIdOrderBySortOrderAsc(place.getId()));
    }

    /*
     여행지 추천. placeId와 category 중 정확히 하나만 받는다.
     - placeId: 음식점·자기 자신 제외 랜덤 5곳 중 가까운 3곳 (거리순)
     - category: 같은 카테고리에서 랜덤 3곳
     */
    @Transactional(readOnly = true)
    public List<PlaceListResponse> getRecommendations(Long placeId, ContentType category) {
        if ((placeId == null) == (category == null)) {
            throw new BusinessException(ErrorCode.RECOMMENDATION_INVALID_PARAMETER);
        }

        List<Place> result = (placeId != null)
                ? recommendByPlace(placeId)
                : placeRepository.findRandomByContentType(category.name());

        return result.stream().map(PlaceListResponse::from).toList();
    }

    private List<Place> recommendByPlace(Long placeId) {
        Place base = placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        List<Place> candidates = placeRepository.findRandomCandidates(ContentType.RESTAURANT.name(), placeId);

        return candidates.stream()
                .sorted(Comparator.comparingDouble(candidate -> distanceMeters(base, candidate)))
                .limit(RECOMMENDATION_SIZE)
                .toList();
    }

    private double distanceMeters(Place from, Place to) {
        double lat1 = Math.toRadians(from.getLatitude().doubleValue());
        double lat2 = Math.toRadians(to.getLatitude().doubleValue());
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(to.getLongitude().doubleValue() - from.getLongitude().doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
