package goormthon.hufs.chulcheck.repository;

import goormthon.hufs.chulcheck.domain.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, Long> {
}
