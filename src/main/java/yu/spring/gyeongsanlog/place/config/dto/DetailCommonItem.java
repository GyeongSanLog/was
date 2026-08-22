package yu.spring.gyeongsanlog.place.config.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 detailCommon2(공통정보 조회) 응답 항목.
 기본 정보는 areaBasedList2에서 이미 받으므로 여기서는 개요와 홈페이지만 쓴다.
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailCommonItem {

    private String contentid;
    private String overview;
    private String homepage;
}
