package goormthon.hufs.chulcheck.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 특정 사용자·클럽에 대한 출석 통계 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAttendanceStatsResponse {
    private long totalCount; // 총 출석 횟수
    private long presentCount; // 정상 출석 횟수
    private long lateCount; // 지각 횟수
    private long absentCount; // 결석 횟수
}