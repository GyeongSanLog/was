package yu.spring.gyeongsanlog.place.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yu.spring.gyeongsanlog.common.dto.SliceResponse;
import yu.spring.gyeongsanlog.common.exception.ErrorResponse;
import yu.spring.gyeongsanlog.place.domain.ContentType;
import yu.spring.gyeongsanlog.place.dto.FavoriteToggleResponse;
import yu.spring.gyeongsanlog.place.dto.PopularPlaceResponse;
import yu.spring.gyeongsanlog.place.dto.PlaceDetailResponse;
import yu.spring.gyeongsanlog.place.dto.PlaceListResponse;
import yu.spring.gyeongsanlog.place.service.FavoriteService;
import yu.spring.gyeongsanlog.place.service.PlaceService;

import java.util.List;

@Tag(name = "area", description = "관광지 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/area")
public class PlaceController {

    private final PlaceService placeService;
    private final FavoriteService favoriteService;

    @Operation(summary = "관광지 목록 조회",
            description = "경산시 관광지를 페이지 단위로 조회한다. type을 주면 해당 유형만 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<SliceResponse<PlaceListResponse>> getPlaces(
            @Parameter(description = "관광지 유형", example = "TOURIST_SPOT")
            @RequestParam(required = false) ContentType type,
            // 이름이 같은 관광지가 있어도 페이지마다 순서가 흔들리지 않도록 id를 보조 정렬키로 둔다
            @ParameterObject @PageableDefault(size = 20, sort = {"name", "id"}, direction = Sort.Direction.ASC)
            Pageable pageable) {

        return ResponseEntity.ok(placeService.getPlaces(type, pageable));
    }

    @Operation(summary = "진행 중인 축제 목록 조회",
            description = "종료되지 않은 축제를 시작일순으로 전체 조회한다. 개수가 적어 페이지네이션이 없다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/festivals")
    public ResponseEntity<List<PlaceListResponse>> getActiveFestivals() {
        return ResponseEntity.ok(placeService.getActiveFestivals());
    }

    @Operation(summary = "중심 관광지 TOP5 조회",
            description = "경산시에서 가장 많이 연결되는 중심 관광지 TOP5를 조회한다. "
                    + "TourAPI 원본 그대로이며 자체 관광지(place)와는 연결되지 않는다. 아직 동기화 전이면 빈 목록.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/popular")
    public ResponseEntity<List<PopularPlaceResponse>> getPopularPlaces() {
        return ResponseEntity.ok(placeService.getPopularPlaces());
    }

    @Operation(summary = "랜덤 관광지 조회", description = "음식점을 제외한 관광지 중 하나를 무작위로 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PlaceDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "조회 가능한 관광지가 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/random")
    public ResponseEntity<PlaceDetailResponse> getRandomPlace() {
        return ResponseEntity.ok(placeService.getRandomPlace());
    }

    @Operation(summary = "찜 목록 조회", description = "최근 찜한 순으로 조회한다(무한스크롤). 인증 필요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/favorites")
    public ResponseEntity<SliceResponse<PlaceListResponse>> getFavorites(
            Authentication authentication,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(favoriteService.getFavorites(userId, pageable));
    }

    @Operation(summary = "여행지 추천",
            description = "placeId(방문한 곳 기준 가까운 3곳) 또는 category(같은 유형 랜덤 3곳) 중 하나만 입력한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "placeId와 category 둘 다 입력했거나 둘 다 비어있음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 관광지",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/recommendations")
    public ResponseEntity<List<PlaceListResponse>> getRecommendations(
            @Parameter(description = "기준이 될 방문 관광지 ID") @RequestParam(required = false) Long placeId,
            @Parameter(description = "기준이 될 카테고리", example = "TOURIST_SPOT")
            @RequestParam(required = false) ContentType category) {

        return ResponseEntity.ok(placeService.getRecommendations(placeId, category));
    }

    @Operation(summary = "관광지 상세 조회", description = "관광지 한 곳의 상세 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PlaceDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 관광지",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceDetailResponse> getPlace(@PathVariable Long placeId) {
        return ResponseEntity.ok(placeService.getPlace(placeId));
    }

    @Operation(summary = "관광지 찜 토글", description = "찜한 상태면 취소, 아니면 찜한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토글 성공",
                    content = @Content(schema = @Schema(implementation = FavoriteToggleResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 관광지",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{placeId}/favorite")
    public ResponseEntity<FavoriteToggleResponse> toggleFavorite(Authentication authentication, @PathVariable Long placeId) {
        Long userId = Long.valueOf(authentication.getName());
        boolean favorited = favoriteService.toggleFavorite(userId, placeId);
        return ResponseEntity.ok(FavoriteToggleResponse.builder().favorited(favorited).build());
    }
}
