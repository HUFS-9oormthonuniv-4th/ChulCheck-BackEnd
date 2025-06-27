package goormthon.hufs.chulcheck.repository;

import goormthon.hufs.chulcheck.domain.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long> {
    
    /**
     * 동아리 이름이나 설명에서 키워드를 포함하는 동아리 검색
     * @param keyword 검색 키워드
     * @return 검색 결과 동아리 목록
     */
    @Query("SELECT c FROM Club c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Club> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(@Param("keyword") String keyword);
    
    /**
     * 동아리 이름으로만 검색
     * @param name 동아리 이름
     * @return 검색 결과 동아리 목록
     */
    List<Club> findByNameContainingIgnoreCase(String name);
    
    /**
     * 동아리 설명으로만 검색  
     * @param description 동아리 설명
     * @return 검색 결과 동아리 목록
     */
    List<Club> findByDescriptionContainingIgnoreCase(String description);
}
