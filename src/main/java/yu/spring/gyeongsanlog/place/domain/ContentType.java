package yu.spring.gyeongsanlog.place.domain;

import lombok.Getter;

import java.util.Arrays;

/*
 detailIntro2(소개정보) 오퍼레이션은 타입마다 응답 필드명이 전부 다르다.
 따라서 동기화 코드가 타입별로 분기하지 않도록 필드명 매핑을 여기서 들고 있는다.
 */
@Getter
public enum ContentType {

    TOURIST_SPOT("12", "관광지", "infocenter", "usetime", "restdate", "parking", null, null, null),
    CULTURAL("14", "문화시설", "infocenterculture", "usetimeculture", "restdateculture", "parkingculture", "usefee", null, null),
    // 축제는 usetimefestival이 '이용요금'이고 운영시간은 playtime이다. 휴무일/주차 필드는 없음
    FESTIVAL("15", "축제", "sponsor1tel", "playtime", null, null, "usetimefestival", "eventstartdate", "eventenddate"),
    // 경산 소재 2곳 모두 detailIntro2 응답이 비어 있어 실측하지 못했다. 명세 기준 필드명
    LEPORTS("28", "레포츠", "infocenterleports", "usetimeleports", "restdateleports", "parkingleports", "usefeeleports", null, null),
    LODGING("32", "숙박", "infocenterlodging", "checkintime", null, "parkinglodging", null, null, null),
    SHOPPING("38", "쇼핑", "infocentershopping", "opentime", "restdateshopping", "parkingshopping", null, null, null),
    RESTAURANT("39", "음식점", "infocenterfood", "opentimefood", "restdatefood", "parkingfood", null, null, null);

    private final String code;
    private final String label;
    private final String telField;
    private final String useTimeField;
    private final String restDateField;
    private final String parkingField;
    private final String useFeeField;
    private final String eventStartDateField;
    private final String eventEndDateField;

    ContentType(String code, String label, String telField, String useTimeField,
                String restDateField, String parkingField, String useFeeField,
                String eventStartDateField, String eventEndDateField) {
        this.code = code;
        this.label = label;
        this.telField = telField;
        this.useTimeField = useTimeField;
        this.restDateField = restDateField;
        this.parkingField = parkingField;
        this.useFeeField = useFeeField;
        this.eventStartDateField = eventStartDateField;
        this.eventEndDateField = eventEndDateField;
    }

    public static ContentType from(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 콘텐츠 타입입니다: " + code));
    }

    // 프론트가 enum 이름(TOURIST_SPOT) 대신 한글 라벨(관광지)로 넘겨도 받을 수 있게 한다
    public static ContentType fromLabel(String label) {
        return Arrays.stream(values())
                .filter(type -> type.label.equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 카테고리입니다: " + label));
    }
}
