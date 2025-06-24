package goormthon.hufs.chulcheck.controller;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.request.ClubJoinRequestDto;
import goormthon.hufs.chulcheck.domain.dto.request.ProcessJoinRequestDto;
import goormthon.hufs.chulcheck.domain.dto.response.ClubJoinRequestResponse;
import goormthon.hufs.chulcheck.domain.dto.response.ClubMemberResponse;
import goormthon.hufs.chulcheck.domain.entity.ClubJoinRequest;
import goormthon.hufs.chulcheck.domain.entity.ClubMember;
import goormthon.hufs.chulcheck.service.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Club Join Request", description = "동아리 가입 요청 관리 API")
@RequestMapping("/api/v1/clubs")
@RestController
@RequiredArgsConstructor
public class ClubJoinRequestController {
    
    private final ClubService clubService;

    @Operation(summary = "동아리 가입 요청", description = "특정 동아리에 가입 요청을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "가입 요청 생성 성공"),
        @ApiResponse(responseCode = "409", description = "이미 멤버이거나 대기중인 요청이 있음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{clubId}/join-requests")
    public ResponseEntity<?> createJoinRequest(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            @Valid @RequestBody ClubJoinRequestDto request,
            Authentication authentication) {
        String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();

        try {
            ClubJoinRequest joinRequest = clubService.createJoinRequest(clubId, userId, request.getMessage());
            ClubJoinRequestResponse response = ClubJoinRequestResponse.fromEntity(joinRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "가입 요청 생성 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "가입 요청 목록 조회 (관리자)", description = "동아리의 모든 가입 요청을 조회합니다. (관리자 권한 필요)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{clubId}/join-requests")
    public ResponseEntity<?> getJoinRequests(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            Authentication authentication) {
        String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();

        try {
            List<ClubJoinRequest> joinRequests = clubService.getJoinRequests(clubId, userId);
            List<ClubJoinRequestResponse> responses = ClubJoinRequestResponse.fromEntityList(joinRequests);
            return ResponseEntity.ok(responses);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "가입 요청 목록 조회 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "대기중인 가입 요청 조회 (관리자)", description = "동아리의 대기중인 가입 요청만 조회합니다. (관리자 권한 필요)")
    @GetMapping("/{clubId}/join-requests/pending")
    public ResponseEntity<?> getPendingJoinRequests(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            Authentication authentication) {
        String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();

        try {
            List<ClubJoinRequest> joinRequests = clubService.getPendingJoinRequests(clubId, userId);
            List<ClubJoinRequestResponse> responses = ClubJoinRequestResponse.fromEntityList(joinRequests);
            return ResponseEntity.ok(responses);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "대기중인 가입 요청 조회 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "내 가입 요청 목록 조회", description = "현재 사용자의 모든 가입 요청을 조회합니다.")
    @GetMapping("/my-join-requests")
    public ResponseEntity<?> getMyJoinRequests(Authentication authentication) {
        String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();

        try {
            List<ClubJoinRequest> joinRequests = clubService.getUserJoinRequests(userId);
            List<ClubJoinRequestResponse> responses = ClubJoinRequestResponse.fromEntityList(joinRequests);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "가입 요청 목록 조회 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "가입 요청 처리", description = "가입 요청을 승인하거나 거절합니다. (관리자 권한 필요)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "처리 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "409", description = "이미 처리된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{clubId}/join-requests/{requestId}/process")
    public ResponseEntity<?> processJoinRequest(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            @Parameter(description = "가입 요청 ID") @PathVariable Long requestId,
            @Valid @RequestBody ProcessJoinRequestDto request,
            Authentication authentication) {
        String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();

        try {
            if (request.getStatus() == goormthon.hufs.chulcheck.domain.enums.ClubStatus.ACTIVE) {
                // 승인
                ClubMember newMember = clubService.approveJoinRequest(requestId, userId);
                ClubMemberResponse response = ClubMemberResponse.fromEntity(newMember);
                return ResponseEntity.ok(Map.of(
                    "message", "가입 요청이 승인되었습니다.",
                    "member", response
                ));
            } else if (request.getStatus() == goormthon.hufs.chulcheck.domain.enums.ClubStatus.REJECTED) {
                // 거절
                ClubJoinRequest rejectedRequest = clubService.rejectJoinRequest(
                    requestId, userId, request.getRejectionReason());
                ClubJoinRequestResponse response = ClubJoinRequestResponse.fromEntity(rejectedRequest);
                return ResponseEntity.ok(Map.of(
                    "message", "가입 요청이 거절되었습니다.",
                    "request", response
                ));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "올바르지 않은 처리 상태입니다. ACTIVE(승인) 또는 REJECTED(거절)만 가능합니다."));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "가입 요청 처리 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "가입 요청 취소", description = "본인의 가입 요청을 취소합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "취소 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인 요청이 아님)"),
        @ApiResponse(responseCode = "409", description = "대기중인 요청이 아님"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/join-requests/{requestId}")
    public ResponseEntity<?> cancelJoinRequest(
            @Parameter(description = "가입 요청 ID") @PathVariable Long requestId,
            Authentication authentication) {
        String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();

        try {
            clubService.cancelJoinRequest(requestId, userId);
            return ResponseEntity.ok(Map.of("message", "가입 요청이 취소되었습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "가입 요청 취소 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "대기중인 가입 요청 개수 조회", description = "동아리의 대기중인 가입 요청 개수를 조회합니다. (관리자 권한 필요)")
    @GetMapping("/{clubId}/join-requests/pending/count")
    public ResponseEntity<?> getPendingJoinRequestCount(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            Authentication authentication) {
        String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();

        try {
            if (!clubService.isClubAdministrator(clubId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "동아리 관리자만 조회할 수 있습니다."));
            }
            
            Long count = clubService.getPendingJoinRequestCount(clubId);
            return ResponseEntity.ok(Map.of("pendingCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "대기중인 가입 요청 개수 조회 중 오류가 발생했습니다."));
        }
    }
}
