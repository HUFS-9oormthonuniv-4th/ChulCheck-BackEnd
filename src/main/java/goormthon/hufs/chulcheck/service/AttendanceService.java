package goormthon.hufs.chulcheck.service;

import goormthon.hufs.chulcheck.domain.dto.response.GetAttendanceResponse;
import goormthon.hufs.chulcheck.domain.dto.response.GetAttendanceStatsResponse;
import goormthon.hufs.chulcheck.domain.entity.Attendance;
import goormthon.hufs.chulcheck.domain.entity.AttendanceSession;
import goormthon.hufs.chulcheck.domain.entity.ClubMember;
import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.domain.enums.AttendanceStatus;
import goormthon.hufs.chulcheck.domain.enums.ClubRole;
import goormthon.hufs.chulcheck.repository.AttendanceRepository;
import goormthon.hufs.chulcheck.repository.AttendanceSessionRepository;
import goormthon.hufs.chulcheck.repository.ClubMemberRepository;
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
    private final ClubMemberRepository clubMemberRepository;

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
     * 개별 사용자의 출석 상태 변경
     * 관리자가 특정 사용자의 출석 상태를 수동으로 변경할 때 사용
     */
    @Transactional
    public Attendance updateAttendanceStatus(Long attendanceId, String statusStr, String adminUserId) {
        // 출석 기록 조회
        Attendance attendance = attendanceRepository.findById(attendanceId)
            .orElseThrow(() -> new EntityNotFoundException("출석 기록을 찾을 수 없습니다: " + attendanceId));
        
        // 상태 값 검증 및 변환
        AttendanceStatus newStatus;
        try {
            newStatus = AttendanceStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("올바르지 않은 출석 상태입니다. PRESENT, LATE, ABSENT 중 하나를 입력해주세요.");
        }
        
        // 관리자 권한 확인
        Long clubId = attendance.getAttendanceSession().getClub().getId();
        ClubMember adminMember = clubMemberRepository.findByClubIdAndUserUserId(clubId, adminUserId)
            .orElseThrow(() -> new SecurityException("해당 동아리에 속하지 않습니다."));
        
        if (adminMember.getRole() != ClubRole.ROLE_MANAGER) {
            throw new SecurityException("동아리 관리자만 출석 상태를 변경할 수 있습니다.");
        }
        
        // 출석 상태 변경
        attendance.setStatus(newStatus);
        attendance.setAttendanceTime(LocalDateTime.now());
        
        Attendance savedAttendance = attendanceRepository.save(attendance);
        
        log.info("출석 상태 변경 완료: attendanceId={}, newStatus={}, updatedBy={}", 
                attendanceId, newStatus, adminUserId);
        
        return savedAttendance;
    }

    /**
     * 특정 세션의 모든 출석을 '출석'으로 일괄 변경
     * 관리자가 세션 종료 후 일괄 출석 처리할 때 사용
     */
    @Transactional
    public List<Attendance> markAllAsPresent(Long sessionId, String userId) {
        // 세션 존재 확인
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("출석 세션을 찾을 수 없습니다: " + sessionId));
        
        // 사용자가 해당 동아리의 관리자인지 확인
        ClubMember member = clubMemberRepository.findByClubIdAndUserUserId(session.getClub().getId(), userId)
            .orElseThrow(() -> new SecurityException("해당 동아리에 속하지 않습니다."));
        
        if (member.getRole() != ClubRole.ROLE_MANAGER) {
            throw new SecurityException("동아리 관리자만 출석을 일괄 변경할 수 있습니다.");
        }
        
        // 해당 세션의 모든 출석 기록 조회
        List<Attendance> attendances = attendanceRepository.findAllByAttendanceSessionId(sessionId);
        
        if (attendances.isEmpty()) {
            throw new EntityNotFoundException("해당 세션에 출석 기록이 없습니다: " + sessionId);
        }
        
        // 모든 출석을 '출석'으로 변경
        LocalDateTime now = LocalDateTime.now();
        for (Attendance attendance : attendances) {
            attendance.setStatus(AttendanceStatus.PRESENT);
            attendance.setAttendanceTime(now);
        }
        
        List<Attendance> savedAttendances = attendanceRepository.saveAll(attendances);
        
        log.info("세션 {}의 모든 출석({})을 '출석'으로 일괄 변경 완료 by user {}", sessionId, attendances.size(), userId);
        
        return savedAttendances;
    }

    /**
     * 특정 세션의 모든 출석을 '결석'으로 일괄 변경
     * 관리자가 세션 종료 후 일괄 결석 처리할 때 사용
     */
    @Transactional
    public List<Attendance> markAllAsAbsent(Long sessionId, String userId) {
        // 세션 존재 확인
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("출석 세션을 찾을 수 없습니다: " + sessionId));
        
        // 사용자가 해당 동아리의 관리자인지 확인
        ClubMember member = clubMemberRepository.findByClubIdAndUserUserId(session.getClub().getId(), userId)
            .orElseThrow(() -> new SecurityException("해당 동아리에 속하지 않습니다."));
        
        if (member.getRole() != ClubRole.ROLE_MANAGER) {
            throw new SecurityException("동아리 관리자만 출석을 일괄 변경할 수 있습니다.");
        }
        
        // 해당 세션의 모든 출석 기록 조회
        List<Attendance> attendances = attendanceRepository.findAllByAttendanceSessionId(sessionId);
        
        if (attendances.isEmpty()) {
            throw new EntityNotFoundException("해당 세션에 출석 기록이 없습니다: " + sessionId);
        }
        
        // 모든 출석을 '결석'으로 변경
        LocalDateTime now = LocalDateTime.now();
        for (Attendance attendance : attendances) {
            attendance.setStatus(AttendanceStatus.ABSENT);
            attendance.setAttendanceTime(now);
        }
        
        List<Attendance> savedAttendances = attendanceRepository.saveAll(attendances);
        
        log.info("세션 {}의 모든 출석({})을 '결석'으로 일괄 변경 완료 by user {}", sessionId, attendances.size(), userId);
        
        return savedAttendances;
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
