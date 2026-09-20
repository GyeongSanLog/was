package yu.spring.gyeongsanlog.user.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.exception.ErrorCode;
import yu.spring.gyeongsanlog.user.config.dto.KakaoProfileResponse;
import yu.spring.gyeongsanlog.user.config.dto.KakaoTokenResponse;

//카카오 OAuth 서버 호출 클라이언트. 인가 코드를 액세스 토큰으로 교환하고, 그 토큰으로 사용자 프로필을 조회한다.
@Slf4j
@Component
public class KakaoOauthClient {

    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String PROFILE_URL = "https://kapi.kakao.com/v2/user/me";

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;

    public KakaoOauthClient(JsonMapper jsonMapper,
                            @Value("${oauth.kakao.client-id}") String clientId,
                            @Value("${oauth.kakao.client-secret}") String clientSecret) {
        // 기본 RestClient가 아니라 스프링이 설정한 Jackson3 JsonMapper를 물려준다.(TourAPI와 일관성 때문)
        this.restClient = RestClient.builder()
                .configureMessageConverters(converters ->
                        converters.withJsonConverter(new JacksonJsonHttpMessageConverter(jsonMapper)))
                .build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getAccessToken(String authCode, String redirectUrl) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUrl);
        params.add("code", authCode);
        params.add("client_secret", clientSecret);

        KakaoTokenResponse response = restClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, res) -> {
                    log.error("카카오 토큰 발급 실패. status: {}", res.getStatusCode());
                    throw new BusinessException(ErrorCode.KAKAO_TOKEN_FAILED);
                })
                .body(KakaoTokenResponse.class);

        if (response == null || response.getAccessToken() == null) {
            throw new BusinessException(ErrorCode.KAKAO_TOKEN_FAILED);
        }
        return response.getAccessToken();
    }

    public KakaoProfileResponse getProfile(String accessToken) {
        KakaoProfileResponse response = restClient.get()
                .uri(PROFILE_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, res) -> {
                    log.error("카카오 프로필 조회 실패. status: {}", res.getStatusCode());
                    throw new BusinessException(ErrorCode.KAKAO_PROFILE_FAILED);
                })
                .body(KakaoProfileResponse.class);

        if (response == null || response.getId() == null) {
            throw new BusinessException(ErrorCode.KAKAO_PROFILE_FAILED);
        }
        return response;
    }
}
