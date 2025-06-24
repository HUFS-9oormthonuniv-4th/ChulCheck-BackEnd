package goormthon.hufs.chulcheck.service;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.KakaoResponse;
import goormthon.hufs.chulcheck.domain.dto.OAuth2Response;
import goormthon.hufs.chulcheck.domain.dto.OAuth2UserDTO;
import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {
    
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
            log.info("OAuth2 사용자 정보 로드: {}", oAuth2User.getAttributes());

            String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
            OAuth2Response oAuth2Response = getOAuth2Response(registrationId, oAuth2User.getAttributes());
            
            if (oAuth2Response == null) {
                throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 프로바이더: " + registrationId);
            }

            String userId = oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();
            User user = userRepository.findByUserId(userId);

            if (user == null) {
                // 신규 사용자 등록
                user = createNewUser(userId, oAuth2Response);
                log.info("신규 OAuth2 사용자 등록: userId={}", userId);
            } else {
                // 기존 사용자 정보 업데이트
                updateExistingUser(user, oAuth2Response);
                log.info("기존 OAuth2 사용자 정보 업데이트: userId={}", userId);
            }

            OAuth2UserDTO userDTO = OAuth2UserDTO.builder()
                    .userId(user.getUserId())
                    .nickname(user.getNickname())
                    .image(user.getImage())
                    .role(user.getRole())
                    .build();

            return new CustomOAuth2User(userDTO);
            
        } catch (Exception e) {
            log.error("OAuth2 사용자 로드 중 오류 발생", e);
            throw new OAuth2AuthenticationException("OAuth2 인증 실패: " + e.getMessage());
        }
    }
    
    private OAuth2Response getOAuth2Response(String registrationId, Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            return new KakaoResponse(attributes);
        }
        // 다른 OAuth2 프로바이더 추가 시 여기에 구현
        return null;
    }
    
    private User createNewUser(String userId, OAuth2Response oAuth2Response) {
        User newUser = User.builder()
                .userId(userId)
                .nickname(oAuth2Response.getNickname())
                .image(oAuth2Response.getImage())
                .role("ROLE_USER")
                .provider(oAuth2Response.getProvider())
                .providerId(oAuth2Response.getProviderId())
                .build();
        
        return userRepository.save(newUser);
    }
    
    private void updateExistingUser(User user, OAuth2Response oAuth2Response) {
        boolean updated = false;
        
        // 닉네임 변경 확인
        if (!user.getNickname().equals(oAuth2Response.getNickname())) {
            user.setNickname(oAuth2Response.getNickname());
            updated = true;
        }
        
        // 프로필 이미지 변경 확인
        if (user.getImage() == null || !user.getImage().equals(oAuth2Response.getImage())) {
            user.setImage(oAuth2Response.getImage());
            updated = true;
        }
        
        if (updated) {
            userRepository.save(user);
        }
    }
}
