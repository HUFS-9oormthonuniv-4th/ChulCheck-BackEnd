package goormthon.hufs.chulcheck.controller;

import goormthon.hufs.chulcheck.domain.dto.LoginRequest;
import goormthon.hufs.chulcheck.domain.dto.LoginResponse;
import goormthon.hufs.chulcheck.domain.dto.SignupRequest;
import goormthon.hufs.chulcheck.domain.dto.request.PasswordResetRequest;
import goormthon.hufs.chulcheck.domain.dto.request.PasswordResetConfirmRequest;
import goormthon.hufs.chulcheck.domain.dto.response.ApiResponse;
import goormthon.hufs.chulcheck.service.AuthService;
import goormthon.hufs.chulcheck.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "인증 관련 API")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        try {
            authService.signup(request);
            return ResponseEntity.ok().body(Map.of("message", "회원가입이 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "회원가입 처리 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다.")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            LoginResponse loginResponse = authService.login(request);
            
            // JWT 토큰을 쿠키에 저장
            Cookie cookie = createAuthCookie(loginResponse.getToken());
            response.addCookie(cookie);
            
            return ResponseEntity.ok(loginResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("로그인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "로그인 처리 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "JWT 토큰을 무효화합니다.")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // 쿠키 삭제
        Cookie cookie = new Cookie("Authorization", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        
        return ResponseEntity.ok().body(Map.of("message", "로그아웃되었습니다."));
    }
    
    @GetMapping("/validate")
    @Operation(summary = "토큰 검증", description = "현재 토큰의 유효성을 확인합니다.")
    public ResponseEntity<?> validateToken(@CookieValue(value = "Authorization", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "토큰이 없습니다."));
        }
        
        try {
            boolean isValid = authService.validateToken(token);
            if (isValid) {
                return ResponseEntity.ok().body(Map.of("valid", true, "message", "유효한 토큰입니다."));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false, "message", "유효하지 않은 토큰입니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "토큰 검증 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping("/password-reset/request")
    @Operation(summary = "비밀번호 재설정 요청", description = "이메일로 비밀번호 재설정 토큰을 발송합니다.")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        try {
            passwordResetService.createPasswordResetToken(request.getEmail());
            
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(200)
                .message("비밀번호 재설정 이메일이 발송되었습니다")
                .data(null)
                .build();
                
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(400)
                .message(e.getMessage())
                .data(null)
                .build();
                
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("비밀번호 재설정 요청 중 오류 발생", e);
            
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(500)
                .message("비밀번호 재설정 요청 처리 중 오류가 발생했습니다")
                .data(null)
                .build();
                
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/password-reset/confirm")
    @Operation(summary = "비밀번호 재설정", description = "토큰을 사용하여 새로운 비밀번호로 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(200)
                .message("비밀번호가 성공적으로 변경되었습니다")
                .data(null)
                .build();
                
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(400)
                .message(e.getMessage())
                .data(null)
                .build();
                
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("비밀번호 재설정 중 오류 발생", e);
            
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(500)
                .message("비밀번호 재설정 처리 중 오류가 발생했습니다")
                .data(null)
                .build();
                
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    private Cookie createAuthCookie(String token) {
        Cookie cookie = new Cookie("Authorization", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(10 * 60 * 60); // 10시간
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}
