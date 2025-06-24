package goormthon.hufs.chulcheck.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
public class User {
    @Id
    private String userId;
    
    @Column(nullable = true) // OAuth 사용자는 비밀번호가 없을 수 있음
    private String password;
    
    @Column(nullable = false)
    private String nickname;
    
    @Column(nullable = true)
    private String image;
    
    @Column(nullable = false)
    private String role;
    
    @Column(nullable = true)
    private String name;
    
    @Column(nullable = true)
    private String school;
    
    @Column(nullable = true)
    private String major;
    
    @Column(nullable = true)
    private String studentNum;
    
    @Column(nullable = true)
    private String provider; // OAuth 제공자 (kakao, google 등)
    
    @Column(nullable = true)
    private String providerId; // OAuth 제공자의 사용자 ID
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private Boolean isActive = true;

    @Builder
    public User(String userId, String password, String nickname, String image, 
                String role, String name, String school, String major, 
                String studentNum, String provider, String providerId) {
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.image = image;
        this.role = role;
        this.name = name;
        this.school = school;
        this.major = major;
        this.studentNum = studentNum;
        this.provider = provider;
        this.providerId = providerId;
    }
    
    // OAuth 사용자인지 확인
    public boolean isOAuthUser() {
        return this.provider != null && this.providerId != null;
    }
}
