package goormthon.hufs.chulcheck.service;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.KakaoResponse;
import goormthon.hufs.chulcheck.domain.dto.OAuth2Response;
import goormthon.hufs.chulcheck.domain.dto.OAuth2UserDTO;
import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    public OAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        log.info(oAuth2User.toString());
        log.info(oAuth2User.getAttributes().toString());

        OAuth2Response oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());

        String userId = oAuth2Response.getProvider()+"_"+oAuth2Response.getProviderId();
        User existUser = userRepository.findByUserId(userId);

        if (existUser == null) {
            User newUser = new User();
            newUser.setUserId(userId);
            newUser.setNickname(oAuth2Response.getNickname());
            newUser.setImage(oAuth2Response.getImage());
            newUser.setRole("ROLE_USER");
            userRepository.save(newUser);

            OAuth2UserDTO userDTO = new OAuth2UserDTO();
            userDTO.setUserId(userId);
            userDTO.setNickname(oAuth2Response.getNickname());
            userDTO.setImage(oAuth2Response.getImage());
            userDTO.setRole("ROLE_USER");

            return new CustomOAuth2User(userDTO);
        } else {
            existUser.setNickname(oAuth2Response.getNickname());
            existUser.setImage(oAuth2Response.getImage());

            userRepository.save(existUser);

            OAuth2UserDTO userDTO = new OAuth2UserDTO();
            userDTO.setUserId(existUser.getUserId());
            userDTO.setNickname(existUser.getNickname());
            userDTO.setImage(existUser.getImage());
            userDTO.setRole("ROLE_USER");

            return new CustomOAuth2User(userDTO);
        }
    }
}
