package goormthon.hufs.chulcheck.domain.dto.response;

import goormthon.hufs.chulcheck.domain.entity.AttendanceSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSessionResponse {
    private Long id;
    private String sessionName;
    private String description;
    private String place;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String attendanceCode;
    private String qrCodeImage;
    private LocalDateTime createdAt;
    
    // 동아리 정보 (순환 참조 방지를 위해 필요한 정보만)
    private Long clubId;
    private String clubName;
    
    public static AttendanceSessionResponse fromEntity(AttendanceSession session) {
        return AttendanceSessionResponse.builder()
                .id(session.getId())
                .sessionName(session.getSessionName())
                .description(session.getDescription())
                .place(session.getPlace())
                .sessionDate(session.getSessionDate())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .attendanceCode(session.getAttendanceCode())
                .qrCodeImage(session.getQrCodeImage())
                .createdAt(session.getCreatedAt())
                .clubId(session.getClub().getId())
                .clubName(session.getClub().getName())
                .build();
    }
    
    public static List<AttendanceSessionResponse> fromEntityList(List<AttendanceSession> sessions) {
        return sessions.stream()
                .map(AttendanceSessionResponse::fromEntity)
                .toList();
    }
}
