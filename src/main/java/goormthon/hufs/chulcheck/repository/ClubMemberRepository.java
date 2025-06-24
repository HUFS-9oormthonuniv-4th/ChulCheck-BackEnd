package goormthon.hufs.chulcheck.repository;

import goormthon.hufs.chulcheck.domain.entity.ClubMember;
import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.domain.enums.ClubRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    Optional<ClubMember> findByClubIdAndUserUserId(Long clubId, String userId);
    List<ClubMember> findAllByClubId(Long clubId);
    List<ClubMember> findAllByUserUserId(String userId);
    List<ClubMember> findAllByClubIdAndRole(Long clubId, ClubRole role);
}
