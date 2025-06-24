package goormthon.hufs.chulcheck.service;

import goormthon.hufs.chulcheck.domain.dto.request.CreateAttendanceSessionRequest;
import goormthon.hufs.chulcheck.domain.entity.Attendance;
import goormthon.hufs.chulcheck.domain.entity.AttendanceSession;
import goormthon.hufs.chulcheck.domain.entity.Club;
import goormthon.hufs.chulcheck.domain.entity.ClubMember;
import goormthon.hufs.chulcheck.domain.enums.ClubRole;
import goormthon.hufs.chulcheck.repository.AttendanceRepository;
import goormthon.hufs.chulcheck.repository.AttendanceSessionRepository;
import goormthon.hufs.chulcheck.repository.ClubMemberRepository;
import goormthon.hufs.chulcheck.repository.ClubRepository;
import goormthon.hufs.chulcheck.domain.enums.AttendanceStatus;
import goormthon.hufs.chulcheck.utils.AttendanceCodeGenerator;
import goormthon.hufs.chulcheck.utils.QRCodeUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AttendanceSessionService {
    private final AttendanceSessionRepository sessionRepository;
    private final ClubMemberRepository memberRepository;
    private final AttendanceRepository attendanceRepository;
    private final ClubRepository clubRepository;
    private final AttendanceCodeGenerator codeGenerator;
    private final QRCodeUtil qrCodeUtil;

    @Transactional
    public AttendanceSession createSession(CreateAttendanceSessionRequest request, String userId) {
        Club club = clubRepository.findById(request.getClubId())
            .orElseThrow(() -> new EntityNotFoundException("동아리를 찾을 수 없습니다: " + request.getClubId()));
        
        Optional<ClubMember> memberOptional = memberRepository.findByClubIdAndUserUserId(request.getClubId(), userId);
        if (memberOptional.isEmpty() || memberOptional.get().getRole() != ClubRole.ROLE_MANAGER) {
            throw new SecurityException("동아리 관리자만 출석 세션을 생성할 수 있습니다.");
        }
        
        // 시간 문자열을 LocalTime으로 파싱
        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.parse(request.getStartTime());
            endTime = LocalTime.parse(request.getEndTime());
        } catch (Exception e) {
            throw new IllegalArgumentException("시간 형식이 올바르지 않습니다. HH:mm 형식으로 입력해주세요.");
        }
        
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }
        
        String attendanceCode;
        do {
            attendanceCode = codeGenerator.generateCode();
        } while (sessionRepository.findByAttendanceCode(attendanceCode).isPresent());
        
        // 출석 세션 생성
        AttendanceSession session = AttendanceSession.builder()
            .sessionName(request.getSessionName())
            .description(request.getDescription())
            .place(request.getPlace())
            .sessionDate(request.getSessionDate())
            .startTime(startTime)
            .endTime(endTime)
            .attendanceCode(attendanceCode)
            .club(club)
            .build();
        
        // 세션 저장
        AttendanceSession savedSession = sessionRepository.save(session);
        
        // QR 코드 생성
        String qrData = qrCodeUtil.createAttendanceQRData(savedSession.getId(), attendanceCode);
        String qrCodeImage = qrCodeUtil.generateQRCodeImage(qrData);
        savedSession.setQrCodeImage(qrCodeImage);
        
        // QR 코드 이미지를 포함하여 다시 저장
        savedSession = sessionRepository.save(savedSession);
        
        log.info("출석 세션 생성 완료: sessionId={}, clubId={}, code={}", 
                savedSession.getId(), club.getId(), attendanceCode);
        
        return savedSession;
    }

    public AttendanceSession getSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("출석 세션을 찾을 수 없습니다: " + sessionId));
    }

    public List<AttendanceSession> getSessionsByClub(Long clubId) {
        return sessionRepository.findByClubIdOrderBySessionDateDescStartTimeDesc(clubId);
    }

    /**
     * 출석 기본값 결석으로 해서 모두 생성함
     * 모임원 모두 가져와서 출석 기본값 결석으로 생성하게 했음
     * saveAll 때문에 yml에 batch size설정하거나 JPA 쿼리로 바꿔야 할듯 (이대로면 10초 걸릴 수도 있음)
     */
    @Transactional
    public void initializeAttendance(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));

        Long clubId = session.getClub().getId();

        List<ClubMember> members = memberRepository.findAllByClubId(clubId);

        for (var member : members) {
            if (!attendanceRepository.existsByUserUserIdAndAttendanceSessionId(member.getUser().getUserId(), sessionId)) {
                Attendance attendance = Attendance.builder()
                    .user(member.getUser())
                    .attendanceSession(session)
                    .build();
                attendance.setStatus(AttendanceStatus.ABSENT);
                attendance.setAttendanceTime(LocalDateTime.now());
                attendanceRepository.save(attendance);
            }
        }
    }

    /**
     * 출석 세션 수정
     */
    @Transactional
    public AttendanceSession updateSession(Long sessionId, CreateAttendanceSessionRequest request, String userId) {
        AttendanceSession session = getSession(sessionId);
        
        // 사용자가 해당 동아리의 관리자인지 확인
        Optional<ClubMember> memberOptional = memberRepository.findByClubIdAndUserUserId(session.getClub().getId(), userId);
        if (memberOptional.isEmpty() || memberOptional.get().getRole() != ClubRole.ROLE_MANAGER) {
            throw new SecurityException("동아리 관리자만 출석 세션을 수정할 수 있습니다.");
        }
        
        // 시간 문자열을 LocalTime으로 파싱
        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.parse(request.getStartTime());
            endTime = LocalTime.parse(request.getEndTime());
        } catch (Exception e) {
            throw new IllegalArgumentException("시간 형식이 올바르지 않습니다. HH:mm 형식으로 입력해주세요.");
        }
        
        // 시작 시간이 종료 시간보다 빠른지 확인
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }
        
        // 세션 정보 업데이트
        session.setSessionName(request.getSessionName());
        session.setDescription(request.getDescription());
        session.setPlace(request.getPlace());
        session.setSessionDate(request.getSessionDate());
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        
        return sessionRepository.save(session);
    }

    /**
     * 출석 세션 삭제
     */
    @Transactional
    public void deleteSession(Long sessionId, String userId) {
        AttendanceSession session = getSession(sessionId);
        
        // 사용자가 해당 동아리의 관리자인지 확인
        Optional<ClubMember> memberOptional = memberRepository.findByClubIdAndUserUserId(session.getClub().getId(), userId);
        if (memberOptional.isEmpty() || memberOptional.get().getRole() != ClubRole.ROLE_MANAGER) {
            throw new SecurityException("동아리 관리자만 출석 세션을 삭제할 수 있습니다.");
        }
        
        sessionRepository.delete(session);
        log.info("출석 세션 삭제 완료: sessionId={}", sessionId);
    }
}
