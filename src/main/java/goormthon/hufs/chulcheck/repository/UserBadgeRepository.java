package goormthon.hufs.chulcheck.repository;

import goormthon.hufs.chulcheck.domain.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
}
