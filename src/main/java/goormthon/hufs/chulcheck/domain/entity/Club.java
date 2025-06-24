package goormthon.hufs.chulcheck.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

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

	@OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonIgnore  // 순환 참조 방지
	private List<ClubMember> members = new ArrayList<>();

	@Builder
	public Club(String name, String representativeAlias, String memberAlias, String description) {
		this.name = name;
		this.representativeAlias = representativeAlias;
		this.memberAlias = memberAlias;
		this.description = description;
	}
}
