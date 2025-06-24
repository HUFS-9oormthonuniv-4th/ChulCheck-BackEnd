package goormthon.hufs.chulcheck.domain.dto.response;

import goormthon.hufs.chulcheck.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String userId;
    private String nickname;
    private String image;
    private String role;
    private String name;
    private String school;
    private String major;
    private String studentNum;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private Boolean isOAuthUser;
    
    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .image(user.getImage())
                .role(user.getRole())
                .name(user.getName())
                .school(user.getSchool())
                .major(user.getMajor())
                .studentNum(user.getStudentNum())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .isActive(user.getIsActive())
                .isOAuthUser(user.isOAuthUser())
                .build();
    }
    
    public static List<UserResponse> fromEntityList(List<User> users) {
        return users.stream()
                .map(UserResponse::fromEntity)
                .toList();
    }
}
