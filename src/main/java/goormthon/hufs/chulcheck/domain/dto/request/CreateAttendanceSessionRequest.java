package goormthon.hufs.chulcheck.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateAttendanceSessionRequest {
    @NotNull(message = "동아리 ID는 필수입니다.")
    private Long clubId;
    
    @NotBlank(message = "세션 제목은 필수입니다.")
    private String sessionName;
    
    private String description;
    
    @NotBlank(message = "장소는 필수입니다.")
    private String place;
    
    @NotNull(message = "세션 날짜는 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sessionDate;
    
    @NotBlank(message = "시작 시간은 필수입니다.")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "시작 시간은 HH:mm 형식이어야 합니다.")
    private String startTime;
    
    @NotBlank(message = "종료 시간은 필수입니다.")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "종료 시간은 HH:mm 형식이어야 합니다.")
    private String endTime;
}
