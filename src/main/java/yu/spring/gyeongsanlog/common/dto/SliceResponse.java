package yu.spring.gyeongsanlog.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

/*
 무한스크롤용 응답 래퍼.
 Slice를 그대로 내보내면 pageable/sort 같은 내부 구조가 노출되므로 필요한 값만 추린다.
 */
@Getter
@Builder
@Schema(description = "무한스크롤 응답")
public class SliceResponse<T> {

    @Schema(description = "조회된 목록")
    private List<T> content;

    @Schema(description = "현재 페이지 번호(0부터)", example = "0")
    private int page;

    @Schema(description = "페이지당 개수", example = "20")
    private int size;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    public static <T> SliceResponse<T> from(Slice<T> slice) {
        return SliceResponse.<T>builder()
                .content(slice.getContent())
                .page(slice.getNumber())
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .build();
    }
}
