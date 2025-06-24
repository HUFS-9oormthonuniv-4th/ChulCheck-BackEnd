package goormthon.hufs.chulcheck.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String userId;
    private String nickname;
    private String role;
    private String message;
}
