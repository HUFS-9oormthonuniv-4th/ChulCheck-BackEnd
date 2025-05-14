package goormthon.hufs.chulcheck.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class OAuth2UserDTO {
    private String userId;
    private String nickname;
    private String image;
    private String role;

    @Builder
    public OAuth2UserDTO(String userId, String nickname, String image, String role) {
        this.userId = userId;
        this.nickname = nickname;
        this.image = image;
        this.role = role;
    }
}
