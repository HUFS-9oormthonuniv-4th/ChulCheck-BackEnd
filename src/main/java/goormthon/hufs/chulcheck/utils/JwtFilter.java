package goormthon.hufs.chulcheck.utils;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.OAuth2UserDTO;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Authorization 쿠키를 찾음
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<String> authorizationOpt = Arrays.stream(cookies)
                .filter(cookie -> "Authorization".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();

        if (authorizationOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationOpt.get();

        try {
            // 토큰 소멸 시간 검증
            if (jwtUtil.isTokenExpired(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 토큰에서 사용자 정보 획득
            String userId = jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token);

            // OAuth2UserDTO 생성 및 설정
            OAuth2UserDTO userDTO = new OAuth2UserDTO();
            userDTO.setUserId(userId);
            userDTO.setRole(role);

            // CustomOAuth2User 생성
            CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO);

            // 스프링 시큐리티 인증 토큰 생성 및 설정
            Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            // 예외 발생 시 로그 출력 및 인증 정보 제거
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }
}