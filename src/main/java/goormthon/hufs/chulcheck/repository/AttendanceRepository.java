package goormthon.hufs.chulcheck.repository;

import goormthon.hufs.chulcheck.domain.entity.Attendance;
import goormthon.hufs.chulcheck.domain.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserUserIdAndAttendanceSessionId(String userId, Long sessionId);
    List<Attendance> findAllByUserUserIdAndAttendanceSessionClubId(String userId, Long clubId);
    
    /**
     * 특정 사용자의 특정 세션 출석 기록 존재 여부 확인
     */
    boolean existsByUserUserIdAndAttendanceSessionId(String userId, Long sessionId);
    
    /**
     * 특정 세션의 모든 출석 기록 조회
     */
    List<Attendance> findAllByAttendanceSessionId(Long sessionId);
    
    /**
     * 특정 동아리의 특정 사용자 출석 기록 조회
     */
    @Query("SELECT a FROM Attendance a " +
           "WHERE a.user.userId = :userId " +
           "AND a.attendanceSession.club.id = :clubId")
    List<Attendance> findByUserIdAndClubId(@Param("userId") String userId, @Param("clubId") Long clubId);
    
    /**
     * 특정 동아리의 특정 사용자 출석 세션 개수 조회
     */
    @Query("SELECT COUNT(DISTINCT a.attendanceSession.id) FROM Attendance a " +
           "WHERE a.user.userId = :userId " +
           "AND a.attendanceSession.club.id = :clubId " +
           "AND a.status = :status")
    long countAttendedSessionsByUserAndClub(@Param("userId") String userId, 
                                           @Param("clubId") Long clubId, 
                                           @Param("status") AttendanceStatus status);
    
    /**
     * 특정 동아리의 전체 출석 세션 개수 (해당 사용자가 속한 기간)
     */
    @Query("SELECT COUNT(DISTINCT s.id) FROM AttendanceSession s " +
           "WHERE s.club.id = :clubId " +
           "AND s.sessionDate >= (SELECT MIN(cm.joinedAt) FROM ClubMember cm WHERE cm.user.userId = :userId AND cm.club.id = :clubId)")
    long countTotalSessionsForUser(@Param("userId") String userId, @Param("clubId") Long clubId);
}
