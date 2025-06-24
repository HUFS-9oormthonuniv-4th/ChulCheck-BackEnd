package goormthon.hufs.chulcheck.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddAdministratorRequest {
    @NotBlank(message = "사용자 ID는 필수입니다.")
    private String userId;
}
