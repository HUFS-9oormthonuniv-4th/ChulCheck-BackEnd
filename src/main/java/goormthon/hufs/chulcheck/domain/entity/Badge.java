package goormthon.hufs.chulcheck.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, length = 50)
	private String name;
	
	@Column(length = 500)
	private String badgeImage;
	
	@Column(length = 200)
	private String description;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BadgeType badgeType;
	
	public enum BadgeType {
		ATTENDANCE,      // 출석 관련
		PARTICIPATION,   // 참여 관련  
		ACHIEVEMENT,     // 업적 관련
		SPECIAL,         // 특별 뱃지
		ROLE            // 역할 관련
	}
}
