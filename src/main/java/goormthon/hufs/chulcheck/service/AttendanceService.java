package goormthon.hufs.chulcheck.service;

import goormthon.hufs.chulcheck.domain.dto.response.GetAttendanceResponse;
import goormthon.hufs.chulcheck.domain.dto.response.GetAttendanceStatsResponse;
import goormthon.hufs.chulcheck.domain.entity.Attendance;
import goormthon.hufs.chulcheck.domain.entity.AttendanceSession;
import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.domain.enums.AttendanceStatus;
import goormthon.hufs.chulcheck.repository.AttendanceRepository;
import goormthon.hufs.chulcheck.repository.AttendanceSessionRepository;
import goormthon.hufs.chulcheck.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;

    /**
     * QR 코드 스캔을 통한 출석 체크
     * 출석 코드로 세션을 찾고, 해당 사용자의 출석 상태를 업데이트
     */
    @Transactional
    public Attendance createAttendance(String userId, Long sessionId, String attendanceCode) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }

        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("출석 세션을 찾을 수 없습니다: " + sessionId));

        if (!session.getAttendanceCode().equals(attendanceCode)) {
            throw new IllegalArgumentException("출석 코드가 일치하지 않습니다.");
        }

        Attendance attendance = attendanceRepository
            .findByUserUserIdAndAttendanceSessionId(userId, sessionId)
            .orElseThrow(() -> new EntityNotFoundException(
                "출석 기록이 존재하지 않습니다. userId=" + userId + ", sessionId=" + sessionId));

        applyStatusAndTime(attendance, session);
        
        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("출석 체크 완료: userId={}, sessionId={}, status={}", 
                userId, sessionId, savedAttendance.getStatus());
        
        return savedAttendance;
    }

    /**
     * 출석 코드로 직접 출석 체크 (QR 코드 대신 수동 입력)
     */
    @Transactional
    public Attendance checkAttendanceByCode(String userId, String attendanceCode) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }

        AttendanceSession session = attendanceSessionRepository.findByAttendanceCode(attendanceCode)
            .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 출석 코드입니다: " + attendanceCode));

        // 출석 가능 시간 확인
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionStart = LocalDateTime.of(session.getSessionDate(), session.getStartTime());
        LocalDateTime sessionEnd = LocalDateTime.of(session.getSessionDate(), session.getEndTime());
        
        if (now.isBefore(sessionStart)) {
            throw new IllegalStateException("아직 출석 시간이 아닙니다. 시작 시간: " + sessionStart);
        }
        if (now.isAfter(sessionEnd)) {
            throw new IllegalStateException("출석 가능 시간이 지났습니다. 마감 시간: " + sessionEnd);
        }

        Attendance attendance = attendanceRepository
            .findByUserUserIdAndAttendanceSessionId(userId, session.getId())
            .orElseThrow(() -> new EntityNotFoundException(
                "출석 기록이 존재하지 않습니다. userId=" + userId + ", sessionId=" + session.getId()));

        applyStatusAndTime(attendance, session);
        
        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("출석 코드로 출석 체크 완료: userId={}, code={}, status={}", 
                userId, attendanceCode, savedAttendance.getStatus());
        
        return savedAttendance;
    }

    public Attendance getAttendance(Long id) {
        return attendanceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("출석 기록을 찾을 수 없습니다: " + id));
    }

    public List<Attendance> getAllAttendances() {
        return attendanceRepository.findAll();
    }

    public List<GetAttendanceResponse> getAllAttendancesByUserAndClub(String userId, Long clubId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }
        
        List<Attendance> attendances = attendanceRepository.findAllByUserUserIdAndAttendanceSessionClubId(userId, clubId);
        return GetAttendanceResponse.fromEntity(attendances);
    }

    public GetAttendanceStatsResponse getAttendanceStatsByUserAndClub(String userId, Long clubId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }

        List<Attendance> attendances =
                attendanceRepository.findAllByUserUserIdAndAttendanceSessionClubId(userId, clubId);

        long total = attendances.size();
        long present = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();
        long late = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();
        long absent = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        return new GetAttendanceStatsResponse(total, present, late, absent);
    }

    @Transactional
    public Attendance updateAttendance(Long id) {
        Attendance attendance = getAttendance(id);
        applyStatusAndTime(attendance, attendance.getAttendanceSession());
        return attendanceRepository.save(attendance);
    }

    @Transactional
    public void deleteAttendance(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new EntityNotFoundException("출석 기록을 찾을 수 없습니다: " + id);
        }
        attendanceRepository.deleteById(id);
    }

    /**
     * 출석 시간과 상태를 결정
     * 세션 시작 시간 이전 또는 정시: 출석
     * 세션 시작 시간 이후 세션 종료 시간 이전: 지각
     * 세션 종료 시간 이후: 출석 불가
     */
    private void applyStatusAndTime(Attendance attendance, AttendanceSession session) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionStart = LocalDateTime.of(session.getSessionDate(), session.getStartTime());
        LocalDateTime sessionEnd = LocalDateTime.of(session.getSessionDate(), session.getEndTime());

        if (now.isAfter(sessionEnd)) {
            throw new IllegalStateException("출석 가능 시간이 지났습니다. 마감 시간: " + sessionEnd);
        }

        AttendanceStatus status;
        if (now.isBefore(sessionStart) || now.isEqual(sessionStart)) {
            status = AttendanceStatus.PRESENT;
        } else {
            status = AttendanceStatus.LATE;
        }
        
        attendance.setStatus(status);
        attendance.setAttendanceTime(now);
    }
}
