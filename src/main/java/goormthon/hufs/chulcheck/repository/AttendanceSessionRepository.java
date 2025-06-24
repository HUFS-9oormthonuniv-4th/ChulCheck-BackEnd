package goormthon.hufs.chulcheck.repository;

import goormthon.hufs.chulcheck.domain.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    
    /**
     * 출석 코드로 세션 조회
     */
    Optional<AttendanceSession> findByAttendanceCode(String attendanceCode);
    
    /**
     * 동아리별 출석 세션 목록 조회 (최신순)
     */
    @Query("SELECT s FROM AttendanceSession s WHERE s.club.id = :clubId ORDER BY s.sessionDate DESC, s.startTime DESC")
    List<AttendanceSession> findByClubIdOrderBySessionDateDescStartTimeDesc(@Param("clubId") Long clubId);
    
    /**
     * 동아리별 출석 세션 개수
     */
    long countByClubId(Long clubId);
}
