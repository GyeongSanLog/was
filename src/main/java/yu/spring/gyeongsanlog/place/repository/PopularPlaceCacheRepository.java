package yu.spring.gyeongsanlog.place.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;
import yu.spring.gyeongsanlog.place.dto.PopularPlaceResponse;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/*
 중심 관광지 TOP5는 월 1회만 갱신되는 외부 데이터라 DB가 아닌 Redis에 캐싱한다.
 매월 9일에 스케줄러가 갱신하므로, 캐시가 비어 있으면 아직 첫 동기화 전이라는 뜻이다.
 */
@Repository
@RequiredArgsConstructor
public class PopularPlaceCacheRepository {

    private static final String KEY = "popular-place:gyeongsan";
    // 다음 갱신(매월 9일)까지 여유 있게 살아있도록 40일로 잡는다
    private static final Duration TTL = Duration.ofDays(40);

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;

    public void save(List<PopularPlaceResponse> places) {
        redisTemplate.opsForValue().set(KEY, jsonMapper.writeValueAsString(places), TTL);
    }

    public List<PopularPlaceResponse> find() {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY))
                .map(json -> jsonMapper.readValue(json, PopularPlaceResponse[].class))
                .map(List::of)
                .orElseGet(List::of);
    }
}
