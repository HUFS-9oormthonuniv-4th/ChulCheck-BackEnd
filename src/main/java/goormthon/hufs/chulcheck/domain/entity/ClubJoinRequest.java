package goormthon.hufs.chulcheck.domain.entity;

import java.time.LocalDateTime;

import goormthon.hufs.chulcheck.domain.enums.ClubStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class ClubJoinRequest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id")
	private Club club;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(length = 500)
	private String message;

	@Enumerated(EnumType.STRING)
	private ClubStatus status;
	
	private LocalDateTime createdAt;
	private LocalDateTime processedAt; // 처리된 시간
	
	@Column(length = 500)
	private String rejectionReason; // 거절 사유

	@Builder
	public ClubJoinRequest(Club club, User user, String message) {
		this.club = club;
		this.user = user;
		this.message = message;
		this.status = ClubStatus.PENDING;
		this.createdAt = LocalDateTime.now();
	}
	
	// 가입 요청 승인
	public void approve() {
		this.status = ClubStatus.ACTIVE;
		this.processedAt = LocalDateTime.now();
	}
	
	// 가입 요청 거절
	public void reject(String reason) {
		this.status = ClubStatus.REJECTED;
		this.rejectionReason = reason;
		this.processedAt = LocalDateTime.now();
	}
}
