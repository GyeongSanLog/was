package yu.spring.gyeongsanlog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/")
    public String health() {
        return "배포가 성공적으로 완료됐습니다.";
    }
}
