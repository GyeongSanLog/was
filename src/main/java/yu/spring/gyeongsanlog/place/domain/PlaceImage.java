package yu.spring.gyeongsanlog.place.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "place_image", uniqueConstraints =
        @UniqueConstraint(name = "uk_place_image_order", columnNames = {"place_id", "sort_order"}))
public class PlaceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false, length = 500)
    private String originUrl;

    @Column(length = 500)
    private String smallUrl;

    @Column(nullable = false)
    private int sortOrder;

    @Builder
    public PlaceImage(Place place, String originUrl, String smallUrl, int sortOrder) {
        this.place = place;
        this.originUrl = originUrl;
        this.smallUrl = smallUrl;
        this.sortOrder = sortOrder;
    }
}
