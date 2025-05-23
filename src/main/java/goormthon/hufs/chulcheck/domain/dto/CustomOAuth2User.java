package goormthon.hufs.chulcheck.domain.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {
    private final OAuth2UserDTO userDTO;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                return userDTO.getRole();
            }
        });
        return authorities;
    }

    @Override
    public String getName() {
        return userDTO.getNickname();
    }

    public String getUserId(){
        return userDTO.getUserId();
    }

    public String getNickname(){
        return this.getName();
    }

    public String getImage(){
        return userDTO.getImage();
    }
}
