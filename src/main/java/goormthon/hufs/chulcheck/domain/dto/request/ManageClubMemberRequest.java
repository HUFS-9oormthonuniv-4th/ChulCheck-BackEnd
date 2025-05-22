package goormthon.hufs.chulcheck.domain.dto.request;

import goormthon.hufs.chulcheck.domain.enums.ClubRole;
import goormthon.hufs.chulcheck.domain.enums.ClubStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManageClubMemberRequest {
    private String userId;
    private ClubRole role;
    private ClubStatus status;
}