package goormthon.hufs.chulcheck.controller;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.request.CreateAttendanceSessionRequest;
import goormthon.hufs.chulcheck.domain.dto.response.AttendanceSessionResponse;
import goormthon.hufs.chulcheck.domain.entity.AttendanceSession;
import goormthon.hufs.chulcheck.service.AttendanceSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendance-sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attendance Session", description = "출석 세션 관리 API")
public class AttendanceSessionController {
    
    private final AttendanceSessionService attendanceSessionService;
    
    @PostMapping
    @Operation(summary = "출석 세션 생성", description = "QR 코드를 포함한 새로운 출석 세션을 생성합니다.")
    public ResponseEntity<?> createSession(
            @Valid @RequestBody CreateAttendanceSessionRequest request,
            Authentication authentication) {
        try {
            String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();
            AttendanceSession session = attendanceSessionService.createSession(request, userId);
            
            // 출석 기록 초기화 (모든 멤버를 결석으로 초기화)
            attendanceSessionService.initializeAttendance(session.getId());
            
            AttendanceSessionResponse response = AttendanceSessionResponse.fromEntity(session);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("출석 세션 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "출석 세션 생성 중 오류가 발생했습니다."));
        }
    }
    
    @GetMapping("/{sessionId}")
    @Operation(summary = "출석 세션 조회", description = "특정 출석 세션의 상세 정보를 조회합니다.")
    public ResponseEntity<?> getSession(@PathVariable Long sessionId) {
        try {
            AttendanceSession session = attendanceSessionService.getSession(sessionId);
            AttendanceSessionResponse response = AttendanceSessionResponse.fromEntity(session);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("출석 세션 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "출석 세션 조회 중 오류가 발생했습니다."));
        }
    }
    
    @GetMapping("/club/{clubId}")
    @Operation(summary = "동아리별 출석 세션 목록 조회", description = "특정 동아리의 모든 출석 세션을 조회합니다.")
    public ResponseEntity<?> getSessionsByClub(@PathVariable Long clubId) {
        try {
            List<AttendanceSession> sessions = attendanceSessionService.getSessionsByClub(clubId);
            List<AttendanceSessionResponse> responses = AttendanceSessionResponse.fromEntityList(sessions);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("동아리별 출석 세션 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "출석 세션 조회 중 오류가 발생했습니다."));
        }
    }
    
    @PutMapping("/{sessionId}")
    @Operation(summary = "출석 세션 수정", description = "기존 출석 세션의 정보를 수정합니다.")
    public ResponseEntity<?> updateSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody CreateAttendanceSessionRequest request,
            Authentication authentication) {
        try {
            String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();
            AttendanceSession updatedSession = attendanceSessionService.updateSession(sessionId, request, userId);
            AttendanceSessionResponse response = AttendanceSessionResponse.fromEntity(updatedSession);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("출석 세션 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "출석 세션 수정 중 오류가 발생했습니다."));
        }
    }
    
    @DeleteMapping("/{sessionId}")
    @Operation(summary = "출석 세션 삭제", description = "기존 출석 세션을 삭제합니다.")
    public ResponseEntity<?> deleteSession(
            @PathVariable Long sessionId,
            Authentication authentication) {
        try {
            String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();
            attendanceSessionService.deleteSession(sessionId, userId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("출석 세션 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "출석 세션 삭제 중 오류가 발생했습니다."));
        }
    }
    
    @GetMapping("/{sessionId}/qr")
    @Operation(summary = "QR 코드 조회", description = "출석 세션의 QR 코드 이미지를 조회합니다.")
    public ResponseEntity<?> getQRCode(@PathVariable Long sessionId) {
        try {
            AttendanceSession session = attendanceSessionService.getSession(sessionId);
            return ResponseEntity.ok(Map.of(
                "sessionId", session.getId(),
                "attendanceCode", session.getAttendanceCode(),
                "qrCodeImage", session.getQrCodeImage(),
                "sessionName", session.getSessionName(),
                "place", session.getPlace(),
                "sessionDate", session.getSessionDate(),
                "startTime", session.getStartTime(),
                "endTime", session.getEndTime()
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("QR 코드 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "QR 코드 조회 중 오류가 발생했습니다."));
        }
    }
}
