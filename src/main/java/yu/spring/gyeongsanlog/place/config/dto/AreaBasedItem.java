package yu.spring.gyeongsanlog.place.config.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 areaBasedList2(지역기반 관광정보 조회) 응답 항목.
 JSON 키가 대부분 소문자라 필드명을 키에 그대로 맞췄다.
 lDongRegnCd/lDongSignguCd는 Lombok 게터명(getLDongRegnCd)에서 Jackson이 유추하는
 이름이 키와 달라지므로 @JsonProperty로 못 박는다.
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AreaBasedItem {

    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private String contentid;
    private String contenttypeid;
    private String title;
    private String addr1;
    private String addr2;
    private String mapx;
    private String mapy;
    private String firstimage;
    private String firstimage2;
    private String modifiedtime;

    @JsonProperty("lDongRegnCd")
    private String ldongRegnCd;

    @JsonProperty("lDongSignguCd")
    private String ldongSignguCd;

    /** mapy(위도). 값이 없거나 숫자가 아니면 null */
    @JsonIgnore
    public BigDecimal getLatitude() {
        return toDecimal(mapy);
    }

    /** mapx(경도). 값이 없거나 숫자가 아니면 null */
    @JsonIgnore
    public BigDecimal getLongitude() {
        return toDecimal(mapx);
    }

    @JsonIgnore
    public boolean hasCoordinates() {
        return getLatitude() != null && getLongitude() != null;
    }

    /** modifiedtime은 yyyyMMddHHmmss 형식의 문자열로 온다 */
    @JsonIgnore
    public LocalDateTime getModifiedAt() {
        if (!StringUtils.hasText(modifiedtime)) {
            return null;
        }
        try {
            return LocalDateTime.parse(modifiedtime.trim(), API_DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal toDecimal(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
