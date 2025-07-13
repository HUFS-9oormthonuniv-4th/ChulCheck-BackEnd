package goormthon.hufs.chulcheck.repository;

import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.domain.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    
    @Query("SELECT ub FROM UserBadge ub JOIN FETCH ub.badge WHERE ub.user = :user")
    List<UserBadge> findByUserWithBadge(@Param("user") User user);
    
    boolean existsByUserAndBadge_Name(User user, String badgeName);
    
    long countByUser(User user);
}
