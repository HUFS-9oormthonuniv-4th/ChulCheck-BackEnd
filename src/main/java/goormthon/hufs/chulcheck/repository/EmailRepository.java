package goormthon.hufs.chulcheck.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import goormthon.hufs.chulcheck.domain.entity.Email;

public interface EmailRepository extends JpaRepository<Email, Long> {
}
