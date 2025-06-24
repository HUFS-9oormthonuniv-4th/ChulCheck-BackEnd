package goormthon.hufs.chulcheck.domain.dto.response;

import goormthon.hufs.chulcheck.domain.entity.Club;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ClubDetailResponse {
    
    // 기본 동아리 정보
    private Long clubId;
    private String clubName;
    private String description;
    private String representativeAlias;
    private String memberAlias;
    
    // 통계 정보
    private int memberCount;
    private int attendanceSessionCount;
    
    // 상세 정보 (관리자만 조회 가능)
    private List<ClubMemberDetailDto> members;
    private List<AttendanceSessionSummaryDto> attendanceSessions;
    
    @Data
    @Builder
    public static class ClubMemberDetailDto {
        private String userId;
        private String name;
        private String nickname;
        private String major;
        private String school;
        private String role;
        private LocalDateTime joinedAt;
        private double attendanceRate; // 출석률 (%)
    }
    
    @Data
    @Builder
    public static class AttendanceSessionSummaryDto {
        private Long sessionId;
        private String sessionName;
        private String place;
        private LocalDateTime sessionDateTime;
        private int totalMembers;
        private int attendedMembers;
        private double attendanceRate; // 해당 세션의 출석률 (%)
        private List<AttendanceDetailDto> attendanceDetails;
    }
    
    @Data
    @Builder
    public static class AttendanceDetailDto {
        private String userId;
        private String userName;
        private LocalDateTime attendanceTime;
        private String status; // PRESENT, ABSENT, LATE, etc.
    }
    
    public static ClubDetailResponse fromEntity(Club club, int memberCount, int sessionCount) {
        return ClubDetailResponse.builder()
                .clubId(club.getId())
                .clubName(club.getName())
                .description(club.getDescription())
                .representativeAlias(club.getRepresentativeAlias())
                .memberAlias(club.getMemberAlias())
                .memberCount(memberCount)
                .attendanceSessionCount(sessionCount)
                .build();
    }
}
