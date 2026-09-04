package yu.spring.gyeongsanlog.place.config.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 LocgoHubTarService1(기초지자체 중심 관광지 정보) areaBasedList1 응답 항목.
 hubTatsCd는 KorService2의 contentId와 다른 코드 체계라 place 테이블과 매칭하지 않는다.
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PopularPlaceItem {

    private String hubTatsCd;
    private String hubTatsNm;
    private String hubCtgryLclsNm;
    private String hubCtgryMclsNm;
    private String hubRank;
    private String mapX;
    private String mapY;
}
