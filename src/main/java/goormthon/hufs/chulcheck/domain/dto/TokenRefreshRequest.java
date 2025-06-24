package goormthon.hufs.chulcheck.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequest {
    @NotBlank(message = "Refresh token은 필수입니다.")
    private String refreshToken;
}
