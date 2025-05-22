package goormthon.hufs.chulcheck.domain.dto.response;

import goormthon.hufs.chulcheck.domain.entity.Club;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetClubInfoResponse {
    private Long id;
    private String name;
    private String description;
    private String role;
    private long memberCount;

    @Data
    public static class ClubInfo{
        private Club club;
        private String role;
        private long memberCount;
    }

    public static GetClubInfoResponse fromEntity(ClubInfo clubInfo) {
        return GetClubInfoResponse.builder()
                .id(clubInfo.getClub().getId())
                .name(clubInfo.getClub().getName())
                .description(clubInfo.getClub().getDescription())
                .role(clubInfo.getRole())
                .memberCount(clubInfo.getMemberCount())
                .build();
    }

    public static List<GetClubInfoResponse> fromEntity(List<ClubInfo> clubInfoList) {
        return clubInfoList.stream()
                .map(GetClubInfoResponse::fromEntity)
                .collect(Collectors.toList());
    }

}
