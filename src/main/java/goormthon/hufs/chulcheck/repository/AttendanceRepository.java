package goormthon.hufs.chulcheck.repository;

import goormthon.hufs.chulcheck.domain.entity.Attendance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserUserIdAndAttendanceSessionId(String userId, Long sessionId);
    List<Attendance> findAllByUserUserIdAndAttendanceSessionClubId(String userId, Long clubId);
    
    /**
     * 특정 사용자의 특정 세션 출석 기록 존재 여부 확인
     */
    boolean existsByUserUserIdAndAttendanceSessionId(String userId, Long sessionId);
}
