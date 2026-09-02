package yu.spring.gyeongsanlog.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yu.spring.gyeongsanlog.common.domain.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"}))
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String name;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    // FCM 푸시 발송용 기기 토큰.
    private String fcmToken;

    // null이면 활성 계정. GroupMember/Clip/Letter가 이 User를 계속 참조하므로 행은 지우지 않고 익명화한다.
    private LocalDateTime deletedAt;

    @Builder
    public User(String email, String nickname, String name, String password, String profileImageUrl, Provider provider, String providerId) {
        this.email = email;
        this.nickname = nickname;
        this.name = name;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
        this.provider = provider;
        this.providerId = providerId;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void withdraw() {
        this.deletedAt = LocalDateTime.now();
        this.email = "withdrawn_" + this.id + "@deleted.local";
        this.password = null;
        this.nickname = "탈퇴한 사용자";
        this.name = "탈퇴한 사용자";
        this.profileImageUrl = null;
        this.fcmToken = null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
