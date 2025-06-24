package goormthon.hufs.chulcheck.utils;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final JwtUtil jwtUtil;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    @Value("${app.jwt.expiration-hours:10}")
    private int tokenExpirationHours;
    
    public CustomSuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        try {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            String userId = customOAuth2User.getUserId();

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            GrantedAuthority auth = iterator.next();
            String role = auth.getAuthority();

            // 올바른 시간 계산 (밀리초 단위)
            long expirationMs = tokenExpirationHours * 60L * 60L * 1000L;
            String token = jwtUtil.createToken(userId, role, expirationMs);

            // 쿠키 설정
            response.addCookie(createCookie("Authorization", token));
            
            // 환경 설정에서 읽은 URL로 리다이렉트
            response.sendRedirect(frontendUrl);
            
            log.info("OAuth2 로그인 성공: userId={}, role={}", userId, role);
            
        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 오류 발생", e);
            response.sendRedirect(frontendUrl + "/login?error=true");
        }
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(tokenExpirationHours * 60 * 60); // 초 단위
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS에서만 전송
        cookie.setAttribute("SameSite", "Lax"); // CSRF 공격 방지
        return cookie;
    }
}
