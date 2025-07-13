package goormthon.hufs.chulcheck.repository;

import goormthon.hufs.chulcheck.domain.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    
    Optional<Badge> findByName(String name);
    
    boolean existsByName(String name);
}