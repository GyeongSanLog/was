package yu.spring.gyeongsanlog.common.config;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Component
public class MultipartJacksonJsonHttpMessageConverter extends JacksonJsonHttpMessageConverter {

    // content-type이 octet-stream이어도 jackson이 읽게해줌
    public MultipartJacksonJsonHttpMessageConverter(JsonMapper jsonMapper) {
        super(jsonMapper);
        setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM));
    }

    // response를 만들 때는 기존의 JSON 컨버터가 사용하도록
    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    protected boolean canWrite(MediaType mediaType) {
        return false;
    }
}
