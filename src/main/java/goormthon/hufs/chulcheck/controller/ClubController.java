package goormthon.hufs.chulcheck.controller;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.request.AddAdministratorRequest;
import goormthon.hufs.chulcheck.domain.dto.request.CreateClubRequest;
import goormthon.hufs.chulcheck.domain.dto.request.UpdateClubRequest;
import goormthon.hufs.chulcheck.domain.dto.response.ClubMemberResponse;
import goormthon.hufs.chulcheck.domain.dto.response.ClubResponse;
import goormthon.hufs.chulcheck.domain.dto.response.ClubDetailResponse;
import goormthon.hufs.chulcheck.domain.dto.response.GetClubInfoResponse;
import goormthon.hufs.chulcheck.domain.entity.Club;
import goormthon.hufs.chulcheck.domain.entity.ClubMember;
import goormthon.hufs.chulcheck.service.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Club", description = "동아리 관리 API")
@RequestMapping("/api/v1/clubs")
@RestController
@RequiredArgsConstructor
public class ClubController {
    private final ClubService clubService;

    @Operation(summary = "모든 동아리 조회", description = "현재 사용자가 가입한 모든 동아리 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public List<GetClubInfoResponse> getAllClubs(Authentication authentication) {
        String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();
        return clubService.getClubsByUserId(userId);
    }

    @Operation(summary = "동아리 검색", description = "동아리 이름 또는 설명으로 동아리를 검색합니다. 검색어가 없으면 모든 동아리를 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "검색 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchClubs(
            @Parameter(description = "검색 키워드 (선택사항)") 
            @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            List<Club> clubs = clubService.searchClubs(keyword);
            List<ClubResponse> responses = clubs.stream()
                    .map(ClubResponse::fromEntity)
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "동아리 검색 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "동아리 생성", description = "새로운 동아리를 생성합니다. 생성자는 자동으로 동아리의 소유자가 됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "동아리 생성 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ResponseEntity<?> createClub(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "동아리 생성 정보")
            @RequestBody CreateClubRequest request, 
            Authentication authentication) {
        try {
            String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();
            request.setOwnerId(userId);
            Club createdClub = clubService.createClub(request);
            ClubResponse response = ClubResponse.fromEntity(createdClub);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "동아리 생성 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "동아리 관리자 목록 조회", description = "특정 동아리의 모든 관리자 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{clubId}/administrators")
    public ResponseEntity<?> getClubAdministrators(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId) {
        try {
            List<ClubMember> administrators = clubService.getAdministrators(clubId);
            List<ClubMemberResponse> responses = ClubMemberResponse.fromEntityList(administrators);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "관리자 목록 조회 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "동아리 관리자 추가", description = "특정 동아리에 새로운 관리자를 추가합니다. (기존 관리자 권한 필요)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "관리자 추가 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{clubId}/administrators")
    public ResponseEntity<?> addClubAdministrator(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId, 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "추가할 관리자 정보")
            @Valid @RequestBody AddAdministratorRequest request,
            Authentication authentication) {
        String currentUserId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();
        String newAdminUserId = request.getUserId();

        try {
            ClubMember administrator = clubService.addAdministrator(clubId, newAdminUserId, currentUserId);
            ClubMemberResponse response = ClubMemberResponse.fromEntity(administrator);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "관리자 추가 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "동아리 정보 수정", description = "동아리 정보를 수정합니다. (관리자 권한 필요)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{clubId}")
    public ResponseEntity<?> updateClub(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "수정할 동아리 정보")
            @RequestBody UpdateClubRequest request,
            Authentication authentication) {
        String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();

        try {
            Club updatedClub = clubService.updateClub(clubId, request, userId);
            ClubResponse response = ClubResponse.fromEntity(updatedClub);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "동아리 수정 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "동아리 삭제", description = "동아리를 삭제합니다. (소유자 권한 필요)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{clubId}")
    public ResponseEntity<?> deleteClub(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            Authentication authentication) {
        String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();

        try {
            clubService.deleteClub(clubId, userId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "동아리 삭제 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "동아리 상세 정보 조회", description = "동아리의 상세 정보를 조회합니다. 회원 목록, 출석 세션 정보, 출석률 등 관리자를 위한 상세 정보가 포함됩니다. (관리자 권한 필요)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{clubId}/detail")
    public ResponseEntity<?> getClubDetail(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            Authentication authentication) {
        String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();
        try {
            ClubDetailResponse response = clubService.getClubDetail(clubId, userId);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "동아리 상세 정보 조회 중 오류가 발생했습니다."));
        }
    }
}
