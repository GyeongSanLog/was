package yu.spring.gyeongsanlog.place.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.exception.ErrorCode;
import yu.spring.gyeongsanlog.place.config.dto.AreaBasedItem;
import yu.spring.gyeongsanlog.place.config.dto.DetailCommonItem;
import yu.spring.gyeongsanlog.place.config.dto.DetailImageItem;
import yu.spring.gyeongsanlog.place.config.dto.TourApiResponse;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/*
 한국관광공사 국문 관광정보 서비스(KorService2) 호출 클라이언트.
 개발계정은 일 1,000건 제한이 있으니 호출 횟수를 늘리지 않도록 주의한다.
 */
@Slf4j
@Component
public class TourApiClient {

    private static final String BASE_URL = "https://apis.data.go.kr/B551011/KorService2";
    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 20;

    private final RestClient restClient;
    private final String encodedServiceKey;
    private final String mobileApp;
    private final String mobileOs;
    private final String ldongRegnCd;
    private final String ldongSignguCd;

    public TourApiClient(JsonMapper jsonMapper,
                         @Value("${tour-api.service-key}") String serviceKey,
                         @Value("${tour-api.mobile-app}") String mobileApp,
                         @Value("${tour-api.mobile-os}") String mobileOs,
                         @Value("${tour-api.ldong-regn-cd}") String ldongRegnCd,
                         @Value("${tour-api.ldong-signgu-cd}") String ldongSignguCd) {
        // 기본 RestClient가 아니라 스프링이 설정한 JsonMapper를 물려준다.
        // TourAPI는 결과가 0건일 때 items를 빈 문자열("")로 주는데,
        // application.yml의 accept-empty-string-as-null-object가 이 매퍼에만 적용되기 때문이다.
        this.restClient = RestClient.builder()
                .configureMessageConverters(converters ->
                        converters.withJsonConverter(new JacksonJsonHttpMessageConverter(jsonMapper)))
                .build();
        // 서비스키는 base64라 +, /, = 를 포함한다. 인코딩하지 않고 넘기면 서버가 +를 공백으로
        // 읽어 SERVICE_KEY_IS_NOT_REGISTERED_ERROR가 난다. 여기서 한 번만 인코딩하고
        // URI를 build(true)로 만들어 이중 인코딩을 막는다.
        this.encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);
        this.mobileApp = mobileApp;
        this.mobileOs = mobileOs;
        this.ldongRegnCd = ldongRegnCd;
        this.ldongSignguCd = ldongSignguCd;
    }

    /**
     * 경산시 관광정보 전체 조회. totalCount를 채울 때까지 페이지를 넘긴다.
     */
    public List<AreaBasedItem> fetchPlaces() {
        List<AreaBasedItem> collected = new ArrayList<>();

        for (int page = 1; page <= MAX_PAGES; page++) {
            URI uri = baseRequest("/areaBasedList2")
                    .queryParam("numOfRows", PAGE_SIZE)
                    .queryParam("pageNo", page)
                    .queryParam("lDongRegnCd", ldongRegnCd)
                    .queryParam("lDongSignguCd", ldongSignguCd)
                    .build(true)
                    .toUri();

            TourApiResponse<AreaBasedItem> response =
                    request(uri, "areaBasedList2", new ParameterizedTypeReference<>() {
                    });

            List<AreaBasedItem> items = response.getItemList();
            collected.addAll(items);
            log.info("TourAPI areaBasedList2 호출 성공 (page={}, {}건, totalCount={})",
                    page, items.size(), response.getTotalCount());

            if (items.isEmpty() || collected.size() >= response.getTotalCount()) {
                break;
            }
        }

        return collected;
    }

    /** 개요·홈페이지. 해당 콘텐츠가 없으면 null */
    public DetailCommonItem fetchDetailCommon(String contentId) {
        URI uri = baseRequest("/detailCommon2")
                .queryParam("numOfRows", 1)
                .queryParam("pageNo", 1)
                .queryParam("contentId", contentId)
                .build(true)
                .toUri();

        TourApiResponse<DetailCommonItem> response =
                request(uri, "detailCommon2", new ParameterizedTypeReference<>() {
                });

        List<DetailCommonItem> items = response.getItemList();
        return items.isEmpty() ? null : items.get(0);
    }

    /**
     * 소개정보. 타입마다 응답 필드명이 달라 Map으로 받고,
     * 어떤 키를 읽을지는 {@link yu.spring.gyeongsanlog.place.domain.ContentType}이 정한다.
     */
    public Map<String, String> fetchDetailIntro(String contentId, String contentTypeId) {
        URI uri = baseRequest("/detailIntro2")
                .queryParam("numOfRows", 1)
                .queryParam("pageNo", 1)
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", contentTypeId)
                .build(true)
                .toUri();

        TourApiResponse<Map<String, String>> response =
                request(uri, "detailIntro2", new ParameterizedTypeReference<>() {
                });

        List<Map<String, String>> items = response.getItemList();
        return items.isEmpty() ? Collections.emptyMap() : items.get(0);
    }

    /** 사진 목록(원본+썸네일). 없으면 빈 목록 */
    public List<DetailImageItem> fetchDetailImages(String contentId) {
        URI uri = baseRequest("/detailImage2")
                .queryParam("numOfRows", 20)
                .queryParam("pageNo", 1)
                .queryParam("contentId", contentId)
                .queryParam("imageYN", "Y")
                .build(true)
                .toUri();

        TourApiResponse<DetailImageItem> response =
                request(uri, "detailImage2", new ParameterizedTypeReference<>() {
                });

        return response.getItemList();
    }

    private UriComponentsBuilder baseRequest(String path) {
        return UriComponentsBuilder.fromUriString(BASE_URL)
                .path(path)
                .queryParam("serviceKey", encodedServiceKey)
                .queryParam("MobileOS", mobileOs)
                .queryParam("MobileApp", mobileApp)
                .queryParam("_type", "json");
    }

    private <T> TourApiResponse<T> request(URI uri, String operation,
                                           ParameterizedTypeReference<TourApiResponse<T>> typeRef) {
        TourApiResponse<T> response;
        try {
            response = restClient.get().uri(uri).retrieve().body(typeRef);
        } catch (RestClientException e) {
            log.error("TourAPI {} 호출 실패", operation, e);
            throw new BusinessException(ErrorCode.TOUR_API_FAILED);
        }

        if (response == null) {
            log.error("TourAPI {} 응답 본문이 비어 있습니다.", operation);
            throw new BusinessException(ErrorCode.TOUR_API_FAILED);
        }
        if (!response.isSuccess()) {
            log.error("TourAPI {} 응답 실패: {}", operation, response.getResultMessage());
            throw new BusinessException(ErrorCode.TOUR_API_FAILED);
        }
        return response;
    }
}
