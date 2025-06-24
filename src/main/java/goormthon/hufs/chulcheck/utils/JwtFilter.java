package goormthon.hufs.chulcheck.utils;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.OAuth2UserDTO;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
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
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // 인증이 필요 없는 경로는 필터를 통과
        if (isPublicPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
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
            // 토큰 만료 검증
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("만료된 JWT 토큰: {}", requestURI);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Token expired\",\"message\":\"토큰이 만료되었습니다.\"}");
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
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                customOAuth2User, null, customOAuth2User.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            log.debug("JWT 인증 성공: userId={}, role={}, uri={}", userId, role, requestURI);

        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰", e);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired", "토큰이 만료되었습니다.");
            return;
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 JWT 토큰", e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid token", "유효하지 않은 토큰입니다.");
            return;
        } catch (SignatureException e) {
            log.error("JWT 서명 검증 실패", e);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid signature", "토큰 서명이 유효하지 않습니다.");
            return;
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 JWT 토큰", e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Unsupported token", "지원하지 않는 토큰 형식입니다.");
            return;
        } catch (Exception e) {
            log.error("JWT 처리 중 예상치 못한 오류", e);
            SecurityContextHolder.clearContext();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Token processing error", "토큰 처리 중 오류가 발생했습니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicPath(String requestURI) {
        return requestURI.startsWith("/oauth2/") ||
               requestURI.startsWith("/login") ||
               requestURI.startsWith("/api/auth/") ||
               requestURI.startsWith("/v3/api-docs") ||
               requestURI.startsWith("/swagger-ui") ||
               requestURI.startsWith("/swagger") ||
               requestURI.equals("/");
    }
    
    private void sendErrorResponse(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("{\"error\":\"%s\",\"message\":\"%s\"}", error, message));
    }
}