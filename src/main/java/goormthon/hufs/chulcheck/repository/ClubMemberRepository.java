package goormthon.hufs.chulcheck.repository;

import goormthon.hufs.chulcheck.domain.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
}
