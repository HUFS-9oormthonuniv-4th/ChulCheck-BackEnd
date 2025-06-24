package goormthon.hufs.chulcheck.domain.dto.response;

import goormthon.hufs.chulcheck.domain.entity.Club;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubResponse {
    private Long id;
    private String name;
    private String representativeAlias;
    private String memberAlias;
    private String description;
    private int memberCount;
    
    public static ClubResponse fromEntity(Club club) {
        return ClubResponse.builder()
                .id(club.getId())
                .name(club.getName())
                .representativeAlias(club.getRepresentativeAlias())
                .memberAlias(club.getMemberAlias())
                .description(club.getDescription())
                .memberCount(club.getMembers() != null ? club.getMembers().size() : 0)
                .build();
    }
    
    public static List<ClubResponse> fromEntityList(List<Club> clubs) {
        return clubs.stream()
                .map(ClubResponse::fromEntity)
                .toList();
    }
}
