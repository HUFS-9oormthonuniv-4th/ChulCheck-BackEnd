package goormthon.hufs.chulcheck.service;

import goormthon.hufs.chulcheck.domain.dto.LoginRequest;
import goormthon.hufs.chulcheck.domain.dto.LoginResponse;
import goormthon.hufs.chulcheck.domain.dto.SignupRequest;
import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.repository.UserRepository;
import goormthon.hufs.chulcheck.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Transactional
    public void signup(SignupRequest request) {
        // 사용자 ID 중복 검사
        if (userRepository.findByUserId(request.getUserId()) != null) {
            throw new IllegalArgumentException("이미 존재하는 사용자 ID입니다.");
        }
        
        // 새 사용자 생성
        User user = User.builder()
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getUserId())
                .name(request.getName())
                .school(request.getSchool())
                .major(request.getMajor())
                .studentNum(request.getStudentNum())
                .role("ROLE_USER")
                .build();
        
        userRepository.save(user);
        log.info("새 사용자 등록 완료: userId={}", request.getUserId());
    }
    
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByUserId(request.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
        
        // 비밀번호 검증 (OAuth 사용자는 비밀번호가 없을 수 있음)
        if (user.getPassword() == null) {
            throw new IllegalArgumentException("소셜 로그인으로 가입한 계정입니다. 소셜 로그인을 이용해주세요.");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        
        // JWT 토큰 생성
        long expirationMs = 10L * 60L * 60L * 1000L; // 10시간
        String token = jwtUtil.createToken(user.getUserId(), user.getRole(), expirationMs);
        
        log.info("로그인 성공: userId={}", user.getUserId());
        
        return LoginResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .role(user.getRole())
                .message("로그인에 성공했습니다.")
                .build();
    }
    
    public boolean validateToken(String token) {
        try {
            return !jwtUtil.isTokenExpired(token);
        } catch (Exception e) {
            log.error("토큰 검증 실패", e);
            return false;
        }
    }
}
