package yu.spring.gyeongsanlog.place.config.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 detailImage2(이미지정보 조회) 응답 항목.
 원본(originimgurl)과 썸네일(smallimageurl) URL을 준다.
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailImageItem {

    private String contentid;
    private String originimgurl;
    private String smallimageurl;
}
