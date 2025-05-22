package goormthon.hufs.chulcheck.domain.dto.response;

import goormthon.hufs.chulcheck.domain.entity.Attendance;
import goormthon.hufs.chulcheck.domain.enums.AttendanceStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAttendanceResponse {
    private Long attendanceId;
    private Long sessionId;
    private LocalDateTime sessionDateTime;
    private LocalDateTime attendanceTime;
    private AttendanceStatus status;

    public static GetAttendanceResponse fromEntity(Attendance attendance) {
        return GetAttendanceResponse.builder()
                .attendanceId(attendance.getId())
                .sessionId(attendance.getAttendanceSession().getId())
                .sessionDateTime(attendance.getAttendanceSession().getSessionDateTime())
                .attendanceTime(attendance.getAttendanceTime())
                .status(attendance.getStatus())
                .build();
    }

    public static List<GetAttendanceResponse> fromEntity(List<Attendance> attendance) {
        return attendance.stream()
                .map(GetAttendanceResponse::fromEntity)
                .toList();
    }
}