package goormthon.hufs.chulcheck.config;

import goormthon.hufs.chulcheck.domain.entity.Badge;
import goormthon.hufs.chulcheck.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BadgeDataLoader implements CommandLineRunner {

    private final BadgeRepository badgeRepository;

    @Override
    public void run(String... args) throws Exception {
        if (badgeRepository.count() == 0) {
            log.info("뱃지 초기 데이터를 생성합니다.");
            createInitialBadges();
        } else {
            log.info("뱃지 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
        }
    }

    private void createInitialBadges() {
        Badge[] badges = {
            Badge.builder()
                .name("3주 개근상")
                .description("연속 3주(21일) 동안 한 번도 빠지지 않고 출석한 부원에게 수여")
                .badgeImage("/images/badges/perfect_attendance_3weeks.png")
                .badgeType(Badge.BadgeType.ATTENDANCE)
                .build(),
                
            Badge.builder()
                .name("정시 출석왕")
                .description("한 달간 지각 없이 정시 출석만 한 부원에게 수여")
                .badgeImage("/images/badges/punctuality_king.png")
                .badgeType(Badge.BadgeType.ATTENDANCE)
                .build(),
                
            Badge.builder()
                .name("활동 참여왕")
                .description("월 활동 참여율 90% 이상을 달성한 부원에게 수여")
                .badgeImage("/images/badges/participation_king.png")
                .badgeType(Badge.BadgeType.PARTICIPATION)
                .build(),
                
            Badge.builder()
                .name("뉴비 부원")
                .description("동아리에 가입한 후 첫 출석을 한 신규 부원에게 수여")
                .badgeImage("/images/badges/newbie.png")
                .badgeType(Badge.BadgeType.SPECIAL)
                .build(),
                
            Badge.builder()
                .name("출석의 PRO")
                .description("누적 출석 횟수 100회를 달성한 베테랑 부원에게 수여")
                .badgeImage("/images/badges/attendance_pro.png")
                .badgeType(Badge.BadgeType.ACHIEVEMENT)
                .build(),
                
            Badge.builder()
                .name("동아리 오너")
                .description("동아리 관리자 또는 회장 등 특별 권한을 가진 부원에게 수여")
                .badgeImage("/images/badges/owner.png")
                .badgeType(Badge.BadgeType.ROLE)
                .build(),
                
            Badge.builder()
                .name("출석 Master")
                .description("한 달간 95% 이상의 출석률을 달성한 부원에게 수여")
                .badgeImage("/images/badges/attendance_master.png")
                .badgeType(Badge.BadgeType.ATTENDANCE)
                .build(),
                
            Badge.builder()
                .name("비오는날의 출석자")
                .description("비가 오는 날에도 5회 이상 출석한 열정적인 부원에게 수여")
                .badgeImage("/images/badges/rainy_day_attendee.png")
                .badgeType(Badge.BadgeType.SPECIAL)
                .build()
        };

        for (Badge badge : badges) {
            badgeRepository.save(badge);
            log.info("뱃지 생성: {}", badge.getName());
        }
        
        log.info("총 {}개의 뱃지가 생성되었습니다.", badges.length);
    }
}