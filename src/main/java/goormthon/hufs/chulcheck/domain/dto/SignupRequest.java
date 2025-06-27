package goormthon.hufs.chulcheck.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank(message = "사용자 ID는 필수입니다.")
    @Size(min = 4, max = 20, message = "사용자 ID는 4자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "사용자 ID는 영문자와 숫자만 사용 가능합니다.")
    private String userId;
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$", 
             message = "비밀번호는 영문자, 숫자, 특수문자를 포함해야 합니다.")
    private String password;
    
    private String name;
    private String school;
    private String major;
    private String studentNum;
}
