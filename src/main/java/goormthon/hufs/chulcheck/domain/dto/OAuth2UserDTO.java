package goormthon.hufs.chulcheck.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserDTO {
    private String userId;
    private String nickname;
    private String image;
    private String role;
}
