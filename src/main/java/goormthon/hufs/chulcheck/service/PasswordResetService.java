package goormthon.hufs.chulcheck.service;

import goormthon.hufs.chulcheck.domain.entity.PasswordResetToken;
import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.repository.PasswordResetTokenRepository;
import goormthon.hufs.chulcheck.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {
    
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    private static final int TOKEN_EXPIRY_HOURS = 1;
    
    public void createPasswordResetToken(String email) {
        User user = userRepository.findByUserId(email);
        if (user == null) {
            throw new RuntimeException("해당 이메일로 등록된 사용자가 없습니다");
        }
        
        passwordResetTokenRepository.deleteByUserId(user.getUserId());
        
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS);
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getUserId())
                .expiryDate(expiryDate)
                .build();
        
        passwordResetTokenRepository.save(resetToken);
        
        emailService.sendPasswordResetEmail(email, token);
    }
    
    public void resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByTokenAndUsedFalse(token);
        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("유효하지 않은 토큰입니다");
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        
        if (resetToken.isExpired()) {
            throw new RuntimeException("만료된 토큰입니다");
        }
        
        User user = userRepository.findByUserId(resetToken.getUserId());
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}