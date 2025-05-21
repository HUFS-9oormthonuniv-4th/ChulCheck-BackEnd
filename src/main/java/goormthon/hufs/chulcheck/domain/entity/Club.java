package goormthon.hufs.chulcheck.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Club {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String representativeAlias; // 대표 별명
	private String memberAlias; // 멤버 별명
	private String description;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User owner;

	@Builder
	public Club(String name, String representativeAlias, String memberAlias, String description, User owner) {
		this.name = name;
		this.representativeAlias = representativeAlias;
		this.memberAlias = memberAlias;
		this.description = description;
		this.owner = owner;
	}
}
