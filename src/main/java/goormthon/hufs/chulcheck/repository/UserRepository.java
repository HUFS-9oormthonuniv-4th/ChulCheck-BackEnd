package goormthon.hufs.chulcheck.repository;

import goormthon.hufs.chulcheck.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserId(String userId);
    void deleteUserByUserId(String userId);
}
