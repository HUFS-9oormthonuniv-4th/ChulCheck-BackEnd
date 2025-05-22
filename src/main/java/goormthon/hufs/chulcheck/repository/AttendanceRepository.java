package goormthon.hufs.chulcheck.repository;

import goormthon.hufs.chulcheck.domain.entity.Attendance;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserUserIdAndAttendanceSessionId(String userId, Long sessionId);
}
