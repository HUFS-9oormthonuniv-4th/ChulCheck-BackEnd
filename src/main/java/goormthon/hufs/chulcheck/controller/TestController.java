package goormthon.hufs.chulcheck.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Slf4j
@Tag(name = "Test", description = "테스트용 API")
@CrossOrigin(origins = "*")
public class TestController {
    
    @GetMapping("/cookies")
    @Operation(summary = "쿠키 확인", description = "현재 요청의 모든 쿠키를 확인합니다.")
    public ResponseEntity<?> checkCookies(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Map<String, String> cookieMap = new HashMap<>();
            Arrays.stream(cookies).forEach(cookie -> {
                cookieMap.put(cookie.getName(), cookie.getValue());
                log.info("쿠키 발견: {}={}", cookie.getName(), cookie.getValue());
            });
            result.put("cookies", cookieMap);
        } else {
            result.put("cookies", "쿠키가 없습니다.");
        }
        
        // Authorization 헤더도 확인
        String authHeader = request.getHeader("Authorization");
        result.put("authHeader", authHeader);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/auth")
    @Operation(summary = "인증 테스트", description = "인증이 필요한 엔드포인트 테스트")
    public ResponseEntity<?> testAuth(@CookieValue(value = "Authorization", required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "인증 성공!");
        result.put("token", token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "토큰 없음");
        
        return ResponseEntity.ok(result);
    }
}