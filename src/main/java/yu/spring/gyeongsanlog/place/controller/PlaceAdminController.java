package yu.spring.gyeongsanlog.place.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yu.spring.gyeongsanlog.common.exception.ErrorResponse;
import yu.spring.gyeongsanlog.place.dto.PlaceDetailSyncResult;
import yu.spring.gyeongsanlog.place.dto.PlaceSyncResult;
import yu.spring.gyeongsanlog.place.service.PopularPlaceSyncService;
import yu.spring.gyeongsanlog.place.service.PlaceSyncService;

@Tag(name = "area-admin", description = "관광지 데이터 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/area")
public class PlaceAdminController {

    private final PlaceSyncService placeSyncService;
    private final PopularPlaceSyncService popularPlaceSyncService;

    @Operation(summary = "관광지 기본 정보 동기화",
            description = "TourAPI에서 경산시 관광정보를 받아 place 테이블에 반영한다. 인증 필요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "동기화 성공",
                    content = @Content(schema = @Schema(implementation = PlaceSyncResult.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "TourAPI 호출 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/sync")
    public ResponseEntity<PlaceSyncResult> syncPlaces() {
        return ResponseEntity.ok(placeSyncService.syncBasicInfo());
    }

    @Operation(summary = "관광지 상세정보 동기화",
            description = "개요·전화번호·편의시설·사진을 받아온다. 관광지 1곳당 3회 호출하므로 "
                    + "상세정보를 받은 적이 없거나 그 뒤에 정보가 바뀐 곳만 대상으로 한다. 인증 필요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "동기화 성공",
                    content = @Content(schema = @Schema(implementation = PlaceDetailSyncResult.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "TourAPI 호출 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/sync/detail")
    public ResponseEntity<PlaceDetailSyncResult> syncPlaceDetails() {
        return ResponseEntity.ok(placeSyncService.syncDetails());
    }

    @Operation(summary = "관광지 사진 동기화 (누락분만)",
            description = "사진이 하나도 없는 관광지만 골라 detailImage2를 호출한다. "
                    + "사진 기능을 뒤늦게 추가해 이미 상세정보가 동기화된 기존 관광지를 채워 넣을 때 쓴다. 인증 필요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "동기화 성공",
                    content = @Content(schema = @Schema(implementation = PlaceDetailSyncResult.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "TourAPI 호출 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/sync/image")
    public ResponseEntity<PlaceDetailSyncResult> syncMissingImages() {
        return ResponseEntity.ok(placeSyncService.syncMissingImages());
    }

    @Operation(summary = "무장애 정보 동기화",
            description = "엘리베이터·화장실·유모차 대여 여부를 받아온다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "동기화 성공",
                    content = @Content(schema = @Schema(implementation = PlaceDetailSyncResult.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "TourAPI 호출 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/sync/accessibility")
    public ResponseEntity<PlaceDetailSyncResult> syncAccessibility() {
        return ResponseEntity.ok(placeSyncService.syncAccessibility());
    }

    @Operation(summary = "중심 관광지 TOP5 동기화",
            description = "경산시에서 가장 많이 연결되는 중심 관광지 TOP5를 받아와 Redis에 캐싱한다. "
                    + "매월 9일 자동으로도 돌지만, 즉시 반영이 필요할 때 수동으로 호출한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "동기화 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/sync/popular")
    public ResponseEntity<Void> syncPopularPlaces() {
        popularPlaceSyncService.syncTop5();
        return ResponseEntity.noContent().build();
    }
}
