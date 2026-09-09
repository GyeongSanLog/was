package yu.spring.gyeongsanlog.place.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import yu.spring.gyeongsanlog.place.domain.ContentType;

// @RequestParam ContentType이 enum 이름(TOURIST_SPOT)뿐 아니라 한글 라벨(관광지)도 받을 수 있게 한다.
// 실패하면 IllegalArgumentException -> Spring이 MethodArgumentTypeMismatchException으로 감싸
// GlobalExceptionHandler가 400으로 처리한다.
@Component
public class ContentTypeConverter implements Converter<String, ContentType> {

    @Override
    public ContentType convert(String source) {
        String value = source.trim();
        try {
            return ContentType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return ContentType.fromLabel(value);
        }
    }
}
