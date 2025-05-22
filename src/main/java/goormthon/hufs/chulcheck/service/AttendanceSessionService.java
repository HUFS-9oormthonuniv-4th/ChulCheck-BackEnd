package goormthon.hufs.chulcheck.service;

import goormthon.hufs.chulcheck.domain.entity.Attendance;
import goormthon.hufs.chulcheck.domain.entity.AttendanceSession;
import goormthon.hufs.chulcheck.domain.entity.ClubMember;
import goormthon.hufs.chulcheck.repository.AttendanceRepository;
import goormthon.hufs.chulcheck.repository.AttendanceSessionRepository;
import goormthon.hufs.chulcheck.repository.ClubMemberRepository;
import goormthon.hufs.chulcheck.domain.enums.AttendanceStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceSessionService {
    private final AttendanceSessionRepository sessionRepository;
    private final ClubMemberRepository memberRepository;
    private final AttendanceRepository attendanceRepository;

    /**
     * 출석 기본값 결석으로 해서 모두 생성함
     * 모임원 모두 가져와서 출석 기본값 결석으로 생성하게 했음
     * saveAll 때문에 yml에 batch size설정하거나 JPA 쿼리로 바꿔야 할듯 (이대로면 10초 걸릴 수도 있음)
     */
    @Transactional
    public void initializeAttendance(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));

        Long clubId = session.getClub().getId();

        List<ClubMember> members =
            memberRepository.findAllByClubId(clubId);

        for (var member : members) {
            Attendance attendance = Attendance.builder()
                .user(member.getUser())
                .attendanceSession(session)
                .build();
            attendance.setStatus(AttendanceStatus.ABSENT);
            attendance.setAttendanceTime(LocalDateTime.now());
            attendanceRepository.save(attendance);
        }
    }
}
