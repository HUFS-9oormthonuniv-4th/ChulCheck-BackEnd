package goormthon.hufs.chulcheck.domain.dto.request;

import goormthon.hufs.chulcheck.domain.enums.ClubStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcessJoinRequestDto {
    
    @NotNull(message = "요청 ID는 필수입니다.")
    private Long requestId;
    
    @NotNull(message = "처리 상태는 필수입니다.")
    private ClubStatus status; // ACTIVE(승인) 또는 REJECTED(거절)
    
    private String rejectionReason; // 거절 시 사유 (선택사항)
}
