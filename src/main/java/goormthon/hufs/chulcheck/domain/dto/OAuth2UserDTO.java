package goormthon.hufs.chulcheck.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OAuth2UserDTO {
    private String userId;
    private String nickname;
    private String image;
    private String role;
}
