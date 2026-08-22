package yu.spring.gyeongsanlog.place.config.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/*
 TourAPI 공통 응답 구조.
 { "response": { "header": {...}, "body": { "items": { "item": [...] }, "totalCount": 110 } } }

 결과가 0건이면 items가 객체가 아니라 빈 문자열("")로 오기 때문에
 application.yml에서 accept-empty-string-as-null-object를 켜서 null로 받는다.
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TourApiResponse<T> {

    private static final String SUCCESS_CODE = "0000";

    private Response<T> response;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response<T> {
        private Header header;
        private Body<T> body;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body<T> {
        private Items<T> items;
        private Integer numOfRows;
        private Integer pageNo;
        private Integer totalCount;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items<T> {
        private List<T> item;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return response != null && response.getHeader() != null
                && SUCCESS_CODE.equals(response.getHeader().getResultCode());
    }

    @JsonIgnore
    public String getResultMessage() {
        if (response == null || response.getHeader() == null) {
            return "응답 본문이 비어 있습니다.";
        }
        return response.getHeader().getResultCode() + " " + response.getHeader().getResultMsg();
    }

    @JsonIgnore
    public List<T> getItemList() {
        if (response == null || response.getBody() == null
                || response.getBody().getItems() == null || response.getBody().getItems().getItem() == null) {
            return Collections.emptyList();
        }
        return response.getBody().getItems().getItem();
    }

    @JsonIgnore
    public int getTotalCount() {
        if (response == null || response.getBody() == null || response.getBody().getTotalCount() == null) {
            return 0;
        }
        return response.getBody().getTotalCount();
    }
}
