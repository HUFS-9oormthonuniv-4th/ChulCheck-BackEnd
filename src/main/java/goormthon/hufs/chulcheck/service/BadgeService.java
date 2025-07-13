package goormthon.hufs.chulcheck.service;

import goormthon.hufs.chulcheck.domain.entity.Badge;
import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.domain.entity.UserBadge;
import goormthon.hufs.chulcheck.repository.BadgeRepository;
import goormthon.hufs.chulcheck.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    public void awardBadge(User user, String badgeName) {
        if (hasUserBadge(user, badgeName)) {
            log.debug("사용자 {}는 이미 '{}' 뱃지를 보유하고 있습니다.", user.getUserId(), badgeName);
            return;
        }

        Optional<Badge> badgeOpt = badgeRepository.findByName(badgeName);
        if (badgeOpt.isEmpty()) {
            log.warn("뱃지를 찾을 수 없습니다: {}", badgeName);
            return;
        }

        Badge badge = badgeOpt.get();
        UserBadge userBadge = UserBadge.builder()
            .user(user)
            .badge(badge)
            .build();

        userBadgeRepository.save(userBadge);
        log.info("사용자 {}에게 '{}' 뱃지를 수여했습니다.", user.getUserId(), badgeName);
    }

    @Transactional(readOnly = true)
    public boolean hasUserBadge(User user, String badgeName) {
        return userBadgeRepository.existsByUserAndBadge_Name(user, badgeName);
    }

    public void checkAndAwardNewbieBadge(User user) {
        awardBadge(user, "뉴비 부원");
    }

    public void checkAndAwardRoleBadge(User user) {
        if (isUserAdmin(user)) {
            awardBadge(user, "동아리 오너");
        }
    }

    private boolean isUserAdmin(User user) {
        return user.getRole() != null && 
               (user.getRole().toString().contains("ADMIN") || 
                user.getRole().toString().contains("OWNER"));
    }
}