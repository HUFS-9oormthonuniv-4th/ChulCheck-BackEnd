package goormthon.hufs.chulcheck.domain.entity;

import java.security.Principal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class User {
    @Id
    private String userId;
    private String nickname;
    private String image;
    private String role;
    private String name;
    private String school;
    private String major;
    private String studentNum;

    @Builder
    public User(String userId, String nickname, String image, String role) {
        this.userId = userId;
        this.nickname = nickname;
        this.image = image;
        this.role = role;
    }
}
