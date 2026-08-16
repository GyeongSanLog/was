package yu.spring.gyeongsanlog.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yu.spring.gyeongsanlog.user.domain.Provider;
import yu.spring.gyeongsanlog.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    boolean existsByEmailAndProvider(String email, Provider provider);
}
