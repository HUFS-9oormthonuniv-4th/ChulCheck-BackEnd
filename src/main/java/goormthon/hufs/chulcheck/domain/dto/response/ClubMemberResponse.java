package goormthon.hufs.chulcheck.domain.dto.response;

import goormthon.hufs.chulcheck.domain.entity.ClubMember;
import goormthon.hufs.chulcheck.domain.enums.ClubRole;
import goormthon.hufs.chulcheck.domain.enums.ClubStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubMemberResponse {
    private Long id;
    private ClubRole role;
    private ClubStatus status;
    private LocalDateTime joinedAt;
    
    // 사용자 정보 (순환 참조 방지를 위해 필요한 정보만)
    private String userId;
    private String nickname;
    private String name;
    private String image;
    
    // 동아리 정보 (순환 참조 방지를 위해 필요한 정보만)
    private Long clubId;
    private String clubName;
    
    public static ClubMemberResponse fromEntity(ClubMember member) {
        return ClubMemberResponse.builder()
                .id(member.getId())
                .role(member.getRole())
                .status(member.getStatus())
                .joinedAt(member.getJoinedAt())
                .userId(member.getUser().getUserId())
                .nickname(member.getUser().getNickname())
                .name(member.getUser().getName())
                .image(member.getUser().getImage())
                .clubId(member.getClub().getId())
                .clubName(member.getClub().getName())
                .build();
    }
    
    public static List<ClubMemberResponse> fromEntityList(List<ClubMember> members) {
        return members.stream()
                .map(ClubMemberResponse::fromEntity)
                .toList();
    }
}
