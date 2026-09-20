package yu.spring.gyeongsanlog.common.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findAllByDeletedTrue();
}
