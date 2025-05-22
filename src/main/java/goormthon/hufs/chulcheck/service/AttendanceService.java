package goormthon.hufs.chulcheck.service;

import goormthon.hufs.chulcheck.domain.dto.response.GetAttendanceResponse;
import goormthon.hufs.chulcheck.domain.entity.Attendance;
import goormthon.hufs.chulcheck.domain.entity.AttendanceSession;
import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.domain.enums.AttendanceStatus;
import goormthon.hufs.chulcheck.repository.AttendanceRepository;
import goormthon.hufs.chulcheck.repository.AttendanceSessionRepository;
import goormthon.hufs.chulcheck.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;

    /**
     * 메서드명은 createAttendance로 했지만 세션 존재 여부 확인하고 기존 Attendance 출석처리함
     * -> AttendanceSessionService에서 initializeAttendance로 Attendance를 결석으로 생성해 둠
     */
    @Transactional
    public Attendance createAttendance(String userId, Long sessionId, String attendanceCode) {
        User user = userRepository.findByUserId(userId);
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("Session이 발견되지 않았습니다: " + sessionId));

        if (!session.getAttendanceCode().equals(attendanceCode)) {
            throw new IllegalArgumentException("인증코드가 일치하지 않습니다.");
        }

        Attendance attendance = attendanceRepository
            .findByUserUserIdAndAttendanceSessionId(userId, sessionId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Attendance가 존재하지 않습니다. userId=" + userId + ", sessionId=" + sessionId));

        applyStatusAndTime(attendance, session);

        return attendanceRepository.save(attendance);
    }

    public Attendance getAttendance(Long id) {
        return attendanceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Attendance가 발견되지 않았습니다." + id));
    }

    public List<Attendance> getAllAttendances() {
        return attendanceRepository.findAll();
    }

    public List<GetAttendanceResponse> getAllAttendancesByUserAndClub(String userId, Long clubId) {
        userRepository.findByUserId(userId);
        List<Attendance> attendances = attendanceRepository.findAllByUserUserIdAndAttendanceSessionClubId(userId, clubId);
        return GetAttendanceResponse.fromEntity(attendances);
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
            throw new EntityNotFoundException("Attendance가 발견되지 않았습니다." + id);
        }
        attendanceRepository.deleteById(id);
    }

    /**
     * 출석 진행 -> 출석시간, 상태 변경
     * 어차피 포인터로 가니까 반환 따로 안함
     */
    private void applyStatusAndTime(Attendance attendance, AttendanceSession session) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate sessionDatePart = session.getSessionDateTime().toLocalDate();
        LocalTime startLocalTime = session.getStartTime().toLocalTime();
        LocalTime endLocalTime = session.getEndTime().toLocalTime();
        LocalDateTime sessionStart = LocalDateTime.of(sessionDatePart, startLocalTime);
        LocalDateTime sessionEnd = LocalDateTime.of(sessionDatePart, endLocalTime);

        if (now.isAfter(sessionEnd)) {
            throw new IllegalStateException("출석 가능 시간이 지났습니다. 마감: " + sessionEnd);
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
