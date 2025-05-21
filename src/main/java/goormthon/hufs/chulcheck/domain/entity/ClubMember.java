package goormthon.hufs.chulcheck.domain.entity;

import java.time.LocalDateTime;

import goormthon.hufs.chulcheck.domain.enums.ClubRole;
import goormthon.hufs.chulcheck.domain.enums.ClubStatus;
import jakarta.persistence.Entity;
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
public class ClubMember {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private Club club;

	@ManyToOne
	private User user;

	private ClubRole role;
	private ClubStatus status;

	private LocalDateTime joinedAt;

	@Builder
	public ClubMember(Club club, User user, ClubRole role, ClubStatus status) {
		this.club = club;
		this.user = user;
		this.role = role;
		this.status = status;
		this.joinedAt = LocalDateTime.now();
	}
}
