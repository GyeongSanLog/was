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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yu.spring.gyeongsanlog.common.dto.SliceResponse;
import yu.spring.gyeongsanlog.common.exception.ErrorResponse;
import yu.spring.gyeongsanlog.place.domain.ContentType;
import yu.spring.gyeongsanlog.place.dto.PlaceDetailResponse;
import yu.spring.gyeongsanlog.place.dto.PlaceListResponse;
import yu.spring.gyeongsanlog.place.service.PlaceService;

@Tag(name = "area", description = "관광지 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/area")
public class PlaceController {

    private final PlaceService placeService;

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
}
