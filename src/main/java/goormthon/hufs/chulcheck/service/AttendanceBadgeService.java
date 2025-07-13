package goormthon.hufs.chulcheck.service;

import goormthon.hufs.chulcheck.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceBadgeService {

    private final BadgeService badgeService;

    public void onFirstAttendance(User user) {
        badgeService.checkAndAwardNewbieBadge(user);
        log.info("첫 출석 이벤트 처리 완료: {}", user.getUserId());
    }

    public void onAttendanceRecord(User user, String attendanceStatus) {
        log.info("출석 기록 이벤트: 사용자={}, 상태={}", user.getUserId(), attendanceStatus);
        
        checkConsecutiveAttendance(user);
        checkMonthlyPunctuality(user);
        checkTotalAttendanceCount(user);
        checkMonthlyAttendanceRate(user);
        checkRainyDayAttendance(user);
    }

    public void onMonthlyParticipationUpdate(User user, double participationRate) {
        if (participationRate >= 0.9) {
            badgeService.awardBadge(user, "활동 참여왕");
            log.info("활동 참여왕 뱃지 수여 조건 충족: {}%", participationRate * 100);
        }
    }

    private void checkConsecutiveAttendance(User user) {
        // TODO: 실제 출석 데이터를 조회하여 연속 3주 출석 여부 확인
        // 현재는 임시로 주석 처리
        log.debug("연속 출석 체크: {}", user.getUserId());
    }

    private void checkMonthlyPunctuality(User user) {
        // TODO: 실제 출석 데이터를 조회하여 한 달간 지각 여부 확인
        log.debug("월별 정시 출석 체크: {}", user.getUserId());
    }

    private void checkTotalAttendanceCount(User user) {
        // TODO: 실제 출석 데이터를 조회하여 총 출석 횟수 확인
        // if (totalAttendanceCount >= 100) {
        //     badgeService.awardBadge(user, "출석의 PRO");
        // }
        log.debug("총 출석 횟수 체크: {}", user.getUserId());
    }

    private void checkMonthlyAttendanceRate(User user) {
        // TODO: 실제 출석 데이터를 조회하여 월별 출석률 확인
        // if (monthlyAttendanceRate >= 0.95) {
        //     badgeService.awardBadge(user, "출석 Master");
        // }
        log.debug("월별 출석률 체크: {}", user.getUserId());
    }

    private void checkRainyDayAttendance(User user) {
        // TODO: 날씨 API와 연동하여 비오는 날 출석 횟수 확인
        // if (rainyDayAttendanceCount >= 5) {
        //     badgeService.awardBadge(user, "비오는날의 출석자");
        // }
        log.debug("비오는 날 출석 체크: {}", user.getUserId());
    }
}