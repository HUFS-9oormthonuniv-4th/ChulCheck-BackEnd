package goormthon.hufs.chulcheck.domain.dto.response;

import goormthon.hufs.chulcheck.domain.entity.Attendance;
import goormthon.hufs.chulcheck.domain.enums.AttendanceStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAttendanceResponse {
    private Long attendanceId;
    private Long sessionId;
    private String sessionName;
    private String place;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime attendanceTime;
    private AttendanceStatus status;
    
    // 사용자 정보 (순환 참조 방지를 위해 필요한 정보만)
    private String userId;
    private String nickname;
    
    // 동아리 정보
    private Long clubId;
    private String clubName;

    public static GetAttendanceResponse fromEntity(Attendance attendance) {
        return GetAttendanceResponse.builder()
                .attendanceId(attendance.getId())
                .sessionId(attendance.getAttendanceSession().getId())
                .sessionName(attendance.getAttendanceSession().getSessionName())
                .place(attendance.getAttendanceSession().getPlace())
                .sessionDate(attendance.getAttendanceSession().getSessionDate())
                .startTime(attendance.getAttendanceSession().getStartTime())
                .endTime(attendance.getAttendanceSession().getEndTime())
                .attendanceTime(attendance.getAttendanceTime())
                .status(attendance.getStatus())
                .userId(attendance.getUser().getUserId())
                .nickname(attendance.getUser().getNickname())
                .clubId(attendance.getAttendanceSession().getClub().getId())
                .clubName(attendance.getAttendanceSession().getClub().getName())
                .build();
    }

    public static List<GetAttendanceResponse> fromEntity(List<Attendance> attendances) {
        return attendances.stream()
                .map(GetAttendanceResponse::fromEntity)
                .toList();
    }
}