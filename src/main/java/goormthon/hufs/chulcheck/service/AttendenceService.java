package goormthon.hufs.chulcheck.service;

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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendenceService {
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;

    @Transactional
    public Attendance createAttendance(Long userId, Long sessionId, String attendanceCode) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User가 발견되지 않았습니다: " + userId));
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("Session이 발견되지 않았습니다: " + sessionId));

        if (!session.getAttendanceCode().equals(attendanceCode)) {
            throw new IllegalArgumentException("인증코드가 일치하지 않습니다.");
        }

        Attendance attendance = Attendance.builder()
            .user(user)
            .attendanceSession(session)
            .build();
        LocalDateTime now = LocalDateTime.now();

        LocalDate sessionDatePart = session.getSessionDate().toLocalDate();
        LocalTime startLocalTime = session.getStartTime().toLocalTime();
        LocalDateTime sessionStart = LocalDateTime.of(sessionDatePart, startLocalTime);

        AttendanceStatus calculatedStatus = now.isBefore(sessionStart) || now.isEqual(sessionStart)
            ? AttendanceStatus.PRESENT
            : AttendanceStatus.LATE;
        attendance.setStatus(calculatedStatus);
        attendance.setAttendanceTime(now);

        return attendanceRepository.save(attendance);
    }

    public Attendance getAttendance(Long id) {
        return attendanceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Attendance가 발견되지 않았습니다." + id));
    }

    public List<Attendance> getAllAttendances() {
        return attendanceRepository.findAll();
    }

    @Transactional
    public Attendance updateAttendance(Long id) {
        Attendance attendance = getAttendance(id);
        LocalDateTime now = LocalDateTime.now();
        LocalDate sessionDatePart = attendance.getAttendanceSession().getSessionDate().toLocalDate();
        LocalTime startLocalTime = attendance.getAttendanceSession().getStartTime().toLocalTime();
        LocalDateTime sessionStart = LocalDateTime.of(sessionDatePart, startLocalTime);
        AttendanceStatus calculatedStatus = now.isBefore(sessionStart) || now.isEqual(sessionStart)
            ? AttendanceStatus.PRESENT
            : AttendanceStatus.LATE;
        attendance.setStatus(calculatedStatus);
        attendance.setAttendanceTime(now);
        return attendanceRepository.save(attendance);
    }

    @Transactional
    public void deleteAttendance(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new EntityNotFoundException("Attendance가 발견되지 않았습니다." + id);
        }
        attendanceRepository.deleteById(id);
    }
}
