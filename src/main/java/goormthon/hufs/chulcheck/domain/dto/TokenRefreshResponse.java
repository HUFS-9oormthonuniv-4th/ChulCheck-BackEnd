package goormthon.hufs.chulcheck.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenRefreshResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
}
