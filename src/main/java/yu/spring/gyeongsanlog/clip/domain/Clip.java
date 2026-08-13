package yu.spring.gyeongsanlog.clip.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yu.spring.gyeongsanlog.common.BaseTimeEntity;
import yu.spring.gyeongsanlog.group.domain.TravelGroup;
import yu.spring.gyeongsanlog.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "clip")
public class Clip extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private TravelGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String videoUrl;

    private String comment;

    @Column(nullable = false)
    private Integer slotIndex;

    @Column(nullable = false)
    private LocalDateTime capturedAt;

    @Builder
    public Clip(TravelGroup group, User user, String videoUrl, String comment, Integer slotIndex, LocalDateTime capturedAt) {
        this.group = group;
        this.user = user;
        this.videoUrl = videoUrl;
        this.comment = comment;
        this.slotIndex = slotIndex;
        this.capturedAt = capturedAt;
    }
}
