package goormthon.hufs.chulcheck.repository;

import goormthon.hufs.chulcheck.domain.entity.ClubJoinRequest;
import goormthon.hufs.chulcheck.domain.entity.Club;
import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.domain.enums.ClubStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubJoinRequestRepository extends JpaRepository<ClubJoinRequest, Long> {
    
    // 특정 동아리의 가입 요청 목록 조회 (상태별)
    List<ClubJoinRequest> findByClubAndStatusOrderByCreatedAtDesc(Club club, ClubStatus status);
    
    // 특정 동아리의 모든 가입 요청 조회
    List<ClubJoinRequest> findByClubOrderByCreatedAtDesc(Club club);
    
    // 특정 사용자의 가입 요청 조회
    List<ClubJoinRequest> findByUserOrderByCreatedAtDesc(User user);
    
    // 사용자가 특정 동아리에 가입 요청을 했는지 확인
    Optional<ClubJoinRequest> findByClubAndUser(Club club, User user);
    
    // 특정 동아리의 대기중인 가입 요청 개수
    @Query("SELECT COUNT(cjr) FROM ClubJoinRequest cjr WHERE cjr.club = :club AND cjr.status = :status")
    Long countByClubAndStatus(@Param("club") Club club, @Param("status") ClubStatus status);
    
    // 사용자가 특정 동아리에 이미 가입 요청을 했는지 확인 (대기중 상태)
    boolean existsByClubAndUserAndStatus(Club club, User user, ClubStatus status);
}
