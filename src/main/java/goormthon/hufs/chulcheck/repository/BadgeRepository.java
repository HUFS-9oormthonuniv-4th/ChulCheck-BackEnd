package goormthon.hufs.chulcheck.repository;

import goormthon.hufs.chulcheck.domain.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
}
