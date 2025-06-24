package goormthon.hufs.chulcheck.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClubJoinRequestDto {
    
    @NotNull(message = "동아리 ID는 필수입니다.")
    private Long clubId;
    
    @NotBlank(message = "가입 메시지는 필수입니다.")
    @Size(max = 500, message = "가입 메시지는 500자를 초과할 수 없습니다.")
    private String message;
}
