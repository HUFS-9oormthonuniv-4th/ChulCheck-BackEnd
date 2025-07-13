package goormthon.hufs.chulcheck.domain.dto.response;

import goormthon.hufs.chulcheck.domain.entity.Badge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeResponse {
    private Long id;
    private String name;
    private String badgeImage;
    private LocalDateTime obtainedAt;
    
    public static BadgeResponse fromBadgeAndUserBadge(Badge badge, LocalDateTime obtainedAt) {
        return BadgeResponse.builder()
                .id(badge.getId())
                .name(badge.getName())
                .badgeImage(badge.getBadgeImage())
                .obtainedAt(obtainedAt)
                .build();
    }
}