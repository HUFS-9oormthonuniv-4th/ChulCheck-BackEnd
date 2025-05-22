package goormthon.hufs.chulcheck.repository;

import goormthon.hufs.chulcheck.domain.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
}
