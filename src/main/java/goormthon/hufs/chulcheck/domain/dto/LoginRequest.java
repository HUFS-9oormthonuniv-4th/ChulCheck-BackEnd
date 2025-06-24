package goormthon.hufs.chulcheck.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "사용자 ID는 필수입니다.")
    private String userId;
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
