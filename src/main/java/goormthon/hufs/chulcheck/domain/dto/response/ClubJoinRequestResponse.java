package goormthon.hufs.chulcheck.domain.dto.response;

import goormthon.hufs.chulcheck.domain.entity.ClubJoinRequest;
import goormthon.hufs.chulcheck.domain.enums.ClubStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class ClubJoinRequestResponse {
    
    private Long id;
    private Long clubId;
    private String clubName;
    private String userId;
    private String userName;
    private String userNickname;
    private String message;
    private ClubStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String rejectionReason;
    
    public static ClubJoinRequestResponse fromEntity(ClubJoinRequest request) {
        return ClubJoinRequestResponse.builder()
                .id(request.getId())
                .clubId(request.getClub().getId())
                .clubName(request.getClub().getName())
                .userId(request.getUser().getUserId())
                .userName(request.getUser().getName())
                .userNickname(request.getUser().getNickname())
                .message(request.getMessage())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .processedAt(request.getProcessedAt())
                .rejectionReason(request.getRejectionReason())
                .build();
    }
    
    public static List<ClubJoinRequestResponse> fromEntityList(List<ClubJoinRequest> requests) {
        return requests.stream()
                .map(ClubJoinRequestResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
