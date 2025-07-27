package goormthon.hufs.chulcheck.controller;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.request.CreateAttendenceRequest;
import goormthon.hufs.chulcheck.domain.dto.response.GetAttendanceResponse;
import goormthon.hufs.chulcheck.domain.dto.response.GetAttendanceStatsResponse;
import goormthon.hufs.chulcheck.domain.entity.Attendance;
import goormthon.hufs.chulcheck.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/api/v1/attendance")
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attendance", description = "출석 관리 API")
@CrossOrigin(origins = "*")
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping
    @Operation(summary = "QR 코드를 통한 출석 체크", description = "QR 코드 스캔으로 출석을 체크합니다.")
    public ResponseEntity<?> attendance(Authentication authentication,
                           @RequestBody CreateAttendenceRequest request) {
        try {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User)authentication.getPrincipal();
            String userId = customOAuth2User.getUserId();
            Attendance attendance = attendanceService.createAttendance(userId, request.getSessionId(), request.getCode());
            
            return ResponseEntity.ok(Map.of(
                "message", "출석 체크가 완료되었습니다.",
                "attendanceId", attendance.getId(),
                "status", attendance.getStatus(),
                "attendanceTime", attendance.getAttendanceTime(),
                "sessionName", attendance.getAttendanceSession().getSessionName(),
                "place", attendance.getAttendanceSession().getPlace()
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("출석 체크 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "출석 체크 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/by-code")
    @Operation(summary = "출석 코드를 통한 출석 체크", description = "출석 코드를 입력하여 출석을 체크합니다.")
    public ResponseEntity<?> attendanceByCode(Authentication authentication,
                                             @RequestBody Map<String, String> request) {
        try {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User)authentication.getPrincipal();
            String userId = customOAuth2User.getUserId();
            String attendanceCode = request.get("code");
            
            if (attendanceCode == null || attendanceCode.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "출석 코드를 입력해주세요."));
            }
            
            Attendance attendance = attendanceService.checkAttendanceByCode(userId, attendanceCode.trim());
            
            return ResponseEntity.ok(Map.of(
                "message", "출석 체크가 완료되었습니다.",
                "status", attendance.getStatus(),
                "attendanceTime", attendance.getAttendanceTime(),
                "sessionName", attendance.getAttendanceSession().getSessionName(),
                "place", attendance.getAttendanceSession().getPlace()
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("출석 코드 체크 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "출석 체크 중 오류가 발생했습니다."));
        }
    }

    @GetMapping
    @Operation(summary = "사용자별 출석 목록 조회", description = "특정 동아리에서 사용자의 모든 출석 기록을 조회합니다.")
    public ResponseEntity<?> getAllAttendances(Authentication authentication,
                                                     @RequestParam Long clubId) {
        try {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User)authentication.getPrincipal();
            String userId = customOAuth2User.getUserId();
            List<GetAttendanceResponse> attendances = attendanceService.getAllAttendancesByUserAndClub(userId, clubId);
            return ResponseEntity.ok(attendances);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("출석 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "출석 목록 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "사용자별 출석 통계 조회", description = "특정 동아리에서 사용자의 출석 통계를 조회합니다.")
    public ResponseEntity<?> getAttendanceStats(Authentication authentication,
                                                         @RequestParam Long clubId) {
        try {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User)authentication.getPrincipal();
            String userId = customOAuth2User.getUserId();
            GetAttendanceStatsResponse stats = attendanceService.getAttendanceStatsByUserAndClub(userId, clubId);
            return ResponseEntity.ok(stats);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("출석 통계 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "출석 통계 조회 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/sessions/{sessionId}/mark-all-present")
    @Operation(summary = "세션 내 모든 출석을 '출석'으로 일괄 변경", description = "관리자가 특정 세션의 모든 사용자를 출석으로 일괄 처리합니다.")
    public ResponseEntity<?> markAllAsPresent(@PathVariable Long sessionId,
                                            Authentication authentication) {
        try {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User)authentication.getPrincipal();
            String userId = customOAuth2User.getUserId();
            
            List<Attendance> attendances = attendanceService.markAllAsPresent(sessionId, userId);
            
            return ResponseEntity.ok(Map.of(
                "message", "모든 출석이 '출석'으로 변경되었습니다.",
                "sessionId", sessionId,
                "updatedCount", attendances.size(),
                "attendances", GetAttendanceResponse.fromEntity(attendances)
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("출석 일괄 변경 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "출석 일괄 변경 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/sessions/{sessionId}/mark-all-absent")
    @Operation(summary = "세션 내 모든 출석을 '결석'으로 일괄 변경", description = "관리자가 특정 세션의 모든 사용자를 결석으로 일괄 처리합니다.")
    public ResponseEntity<?> markAllAsAbsent(@PathVariable Long sessionId,
                                           Authentication authentication) {
        try {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User)authentication.getPrincipal();
            String userId = customOAuth2User.getUserId();
            
            List<Attendance> attendances = attendanceService.markAllAsAbsent(sessionId, userId);
            
            return ResponseEntity.ok(Map.of(
                "message", "모든 출석이 '결석'으로 변경되었습니다.",
                "sessionId", sessionId,
                "updatedCount", attendances.size(),
                "attendances", GetAttendanceResponse.fromEntity(attendances)
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("출석 일괄 변경 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "출석 일괄 변경 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/{attendanceId}/status")
    @Operation(summary = "개별 출석 상태 변경", description = "관리자가 특정 사용자의 출석 상태를 변경합니다. (출석/지각/결석)")
    public ResponseEntity<?> updateAttendanceStatus(@PathVariable Long attendanceId,
                                                   @RequestParam String status,
                                                   Authentication authentication) {
        try {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User)authentication.getPrincipal();
            String userId = customOAuth2User.getUserId();
            
            Attendance updatedAttendance = attendanceService.updateAttendanceStatus(attendanceId, status, userId);
            
            return ResponseEntity.ok(Map.of(
                "message", "출석 상태가 변경되었습니다.",
                "attendanceId", updatedAttendance.getId(),
                "newStatus", updatedAttendance.getStatus(),
                "updatedTime", updatedAttendance.getAttendanceTime(),
                "sessionName", updatedAttendance.getAttendanceSession().getSessionName(),
                "userName", updatedAttendance.getUser().getName()
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("출석 상태 변경 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "출석 상태 변경 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/users/{targetUserId}")
    @Operation(summary = "특정 사용자의 출석 기록 조회 (관리자용)", 
               description = "관리자가 특정 동아리 멤버의 모든 출석 기록을 조회합니다.")
    public ResponseEntity<?> getUserAttendances(
            @PathVariable String targetUserId,
            @RequestParam Long clubId,
            Authentication authentication) {
        try {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User)authentication.getPrincipal();
            String adminUserId = customOAuth2User.getUserId();
            
            List<GetAttendanceResponse> attendances = 
                attendanceService.getUserAttendancesByAdmin(targetUserId, clubId, adminUserId);
            
            return ResponseEntity.ok(Map.of(
                "targetUserId", targetUserId,
                "clubId", clubId,
                "attendances", attendances,
                "totalCount", attendances.size()
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("사용자 출석 기록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "출석 기록 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/users/{targetUserId}/stats")
    @Operation(summary = "특정 사용자의 출석 통계 조회 (관리자용)", 
               description = "관리자가 특정 동아리 멤버의 출석 통계를 조회합니다.")
    public ResponseEntity<?> getUserAttendanceStats(
            @PathVariable String targetUserId,
            @RequestParam Long clubId,
            Authentication authentication) {
        try {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User)authentication.getPrincipal();
            String adminUserId = customOAuth2User.getUserId();
            
            GetAttendanceStatsResponse stats = 
                attendanceService.getUserAttendanceStatsByAdmin(targetUserId, clubId, adminUserId);
            
            return ResponseEntity.ok(stats);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("사용자 출석 통계 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "출석 통계 조회 중 오류가 발생했습니다."));
        }
    }
}
