package goormthon.hufs.chulcheck.domain.entity;

import java.time.LocalDateTime;

import goormthon.hufs.chulcheck.domain.enums.ClubStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

	@ManyToOne
	private Club club;

	@ManyToOne
	private User user;

	private String message;

	@Enumerated(EnumType.STRING)
	private ClubStatus status;
	private LocalDateTime createdAt;

	@Builder
	public ClubJoinRequest(Club club, User user, String message) {
		this.club = club;
		this.user = user;
		this.message = message;
		this.status = ClubStatus.PENDING;
		this.createdAt = LocalDateTime.now();
	}
}
