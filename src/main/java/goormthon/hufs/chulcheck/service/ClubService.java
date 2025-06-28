package goormthon.hufs.chulcheck.service;

import goormthon.hufs.chulcheck.domain.dto.request.CreateClubRequest;
import goormthon.hufs.chulcheck.domain.dto.request.ManageClubMemberRequest;
import goormthon.hufs.chulcheck.domain.dto.request.UpdateClubRequest;
import goormthon.hufs.chulcheck.domain.dto.response.ClubDetailResponse;
import goormthon.hufs.chulcheck.domain.dto.response.GetClubInfoResponse;
import goormthon.hufs.chulcheck.domain.entity.Club;
import goormthon.hufs.chulcheck.domain.entity.ClubMember;
import goormthon.hufs.chulcheck.domain.entity.ClubJoinRequest;
import goormthon.hufs.chulcheck.domain.entity.AttendanceSession;
import goormthon.hufs.chulcheck.domain.entity.Attendance;
import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.domain.enums.ClubRole;
import goormthon.hufs.chulcheck.domain.enums.ClubStatus;
import goormthon.hufs.chulcheck.domain.enums.AttendanceStatus;
import goormthon.hufs.chulcheck.repository.ClubRepository;
import goormthon.hufs.chulcheck.repository.ClubMemberRepository;
import goormthon.hufs.chulcheck.repository.ClubJoinRequestRepository;
import goormthon.hufs.chulcheck.repository.AttendanceSessionRepository;
import goormthon.hufs.chulcheck.repository.AttendanceRepository;
import goormthon.hufs.chulcheck.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import goormthon.hufs.chulcheck.domain.dto.response.GetClubInfoResponse.ClubInfo;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {
    private final ClubRepository clubRepository;
    private final ClubMemberRepository memberRepository;
    private final ClubJoinRequestRepository joinRequestRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final ClubMemberRepository clubMemberRepository;

    @Transactional
    public Club createClub(CreateClubRequest req) {
        User creator = userRepository.findByUserId(req.getOwnerId());
        Club club = Club.builder()
            .name(req.getName())
            .representativeAlias(req.getRepresentativeAlias())
            .memberAlias(req.getMemberAlias())
            .description(req.getDescription())
            .build();
        Club saved = clubRepository.save(club);

        ClubMember adminMember = ClubMember.builder()
            .club(saved)
            .user(creator)
            .role(ClubRole.ROLE_MANAGER)
            .status(ClubStatus.ACTIVE)
            .build();
        memberRepository.save(adminMember);
        return saved;
    }

    public Club getClub(Long clubId) {
        return clubRepository.findById(clubId)
            .orElseThrow(() -> new EntityNotFoundException("Club이 발견되지 않았습니다: " + clubId));
    }

    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    public List<Club> searchClubs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return clubRepository.findAll();
        }
        return clubRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword.trim());
    }

    @Transactional
    public Club updateClub(Long clubId, UpdateClubRequest req, String userId) {
        if (!isClubAdministrator(clubId, userId)) {
            throw new SecurityException("Only administrators can update clubs");
        }

        Club club = getClub(clubId);
        club.setName(req.getName());
        club.setRepresentativeAlias(req.getRepresentativeAlias());
        club.setMemberAlias(req.getMemberAlias());
        club.setDescription(req.getDescription());
        return clubRepository.save(club);
    }

    @Transactional
    public void deleteClub(Long clubId, String userId) {
        if (!isClubAdministrator(clubId, userId)) {
            throw new SecurityException("Only administrators can delete clubs");
        }

        if (!clubRepository.existsById(clubId)) {
            throw new EntityNotFoundException("Club not found: " + clubId);
        }
        clubRepository.deleteById(clubId);
    }

    @Transactional
    public ClubMember addMember(Long clubId, ManageClubMemberRequest req) {
        Club club = getClub(clubId);
        User user = userRepository.findByUserId(req.getUserId());
        if (memberRepository.findByClubIdAndUserUserId(clubId, req.getUserId()).isPresent()) {
            throw new IllegalStateException("Member가 이미 모임에 존재합니다.");
        }
        ClubMember member = ClubMember.builder()
            .club(club)
            .user(user)
            .role(req.getRole())
            .status(req.getStatus())
            .build();
        return memberRepository.save(member);
    }

    @Transactional
    public ClubMember updateMember(Long clubId, ManageClubMemberRequest req) {
        ClubMember member = memberRepository.findByClubIdAndUserUserId(clubId, req.getUserId())
            .orElseThrow(() -> new EntityNotFoundException("해당 User의 모임 가입정보를 찾을 수 없습니다: " + req.getUserId()));
        member.setRole(req.getRole());
        member.setStatus(req.getStatus());
        return memberRepository.save(member);
    }

    @Transactional
    public ClubMember addAdministrator(Long clubId, String newAdminUserId, String currentUserId) {
        if (!isClubAdministrator(clubId, currentUserId)) {
            throw new SecurityException("Only administrators can add new administrators");
        }

        Club club = getClub(clubId);
        User user = userRepository.findByUserId(newAdminUserId);

        Optional<ClubMember> existingMember = memberRepository.findByClubIdAndUserUserId(clubId, newAdminUserId);

        if (existingMember.isPresent()) {
            ClubMember member = existingMember.get();
            member.setRole(ClubRole.ROLE_MANAGER);
            return memberRepository.save(member);
        } else {
            ClubMember newAdmin = ClubMember.builder()
                .club(club)
                .user(user)
                .role(ClubRole.ROLE_MANAGER)
                .status(ClubStatus.ACTIVE)
                .build();
            return memberRepository.save(newAdmin);
        }
    }

    @Transactional
    public ClubMember removeAdministrator(Long clubId, String targetUserId, String currentUserId) {
        if (!isClubAdministrator(clubId, currentUserId)) {
            throw new SecurityException("동아리 관리자만 다른 관리자의 권한을 제거할 수 있습니다.");
        }

        // 대상 멤버 조회
        ClubMember targetMember = memberRepository.findByClubIdAndUserUserId(clubId, targetUserId)
            .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다: " + targetUserId));

        // 대상이 관리자인지 확인
        if (targetMember.getRole() != ClubRole.ROLE_MANAGER) {
            throw new IllegalStateException("해당 사용자는 관리자가 아닙니다.");
        }

        // 자기 자신의 권한은 제거할 수 없음
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("자신의 관리자 권한은 제거할 수 없습니다.");
        }

        // 관리자를 일반 멤버로 변경
        targetMember.setRole(ClubRole.ROLE_MEMBER);
        return memberRepository.save(targetMember);
    }

    @Transactional
    public void removeMember(Long clubId, String userId) {
        ClubMember member = memberRepository.findByClubIdAndUserUserId(clubId, userId)
            .orElseThrow(() -> new EntityNotFoundException("해당 User의 모임 가입정보를 찾을 수 없습니다: " + userId));
        memberRepository.delete(member);
    }

    public List<ClubMember> getMembers(Long clubId) {
        getClub(clubId);
        return memberRepository.findAllByClubId(clubId);
    }

    public List<ClubMember> getAdministrators(Long clubId) {
        getClub(clubId);
        return memberRepository.findAllByClubIdAndRole(clubId, ClubRole.ROLE_MANAGER);
    }

    public List<GetClubInfoResponse> getClubsByUserId(String userId) {
        // 사용자 존재 검증
        userRepository.findByUserId(userId);

        // 사용자가 속한 클럽 멤버십 조회
        List<ClubMember> clubMembers = clubMemberRepository.findAllByUserUserId(userId);

        // 각 멤버십별로 DTO 변환
        return clubMembers.stream()
            .map(clubMember -> {
                var club = clubMember.getClub();
                long memberCount = memberRepository.findAllByClubId(club.getId()).size();
                String roleLabel = clubMember.getRole().name();

                ClubInfo info = new ClubInfo();
                info.setClub(club);
                info.setRole(roleLabel);
                info.setMemberCount(memberCount);
                return GetClubInfoResponse.fromEntity(info);
            })
            .collect(Collectors.toList());
    }

    public boolean isClubAdministrator(Long clubId, String userId) {
        Optional<ClubMember> member = memberRepository.findByClubIdAndUserUserId(clubId, userId);
        return member.isPresent() && member.get().getRole() == ClubRole.ROLE_MANAGER;
    }

    // ===== 가입 요청 관련 메소드들 =====
    
    /**
     * 동아리 가입 요청 생성
     */
    @Transactional
    public ClubJoinRequest createJoinRequest(Long clubId, String userId, String message) {
        Club club = getClub(clubId);
        User user = userRepository.findByUserId(userId);
        
        // 이미 멤버인지 확인
        if (memberRepository.findByClubIdAndUserUserId(clubId, userId).isPresent()) {
            throw new IllegalStateException("이미 해당 동아리의 멤버입니다.");
        }
        
        // 이미 대기중인 요청이 있는지 확인
        if (joinRequestRepository.existsByClubAndUserAndStatus(club, user, ClubStatus.PENDING)) {
            throw new IllegalStateException("이미 대기중인 가입 요청이 있습니다.");
        }
        
        ClubJoinRequest joinRequest = ClubJoinRequest.builder()
                .club(club)
                .user(user)
                .message(message)
                .build();
        
        return joinRequestRepository.save(joinRequest);
    }
    
    /**
     * 동아리의 가입 요청 목록 조회 (관리자용)
     */
    public List<ClubJoinRequest> getJoinRequests(Long clubId, String adminUserId) {
        if (!isClubAdministrator(clubId, adminUserId)) {
            throw new SecurityException("동아리 관리자만 가입 요청을 조회할 수 있습니다.");
        }
        
        Club club = getClub(clubId);
        return joinRequestRepository.findByClubOrderByCreatedAtDesc(club);
    }
    
    /**
     * 대기중인 가입 요청만 조회 (관리자용)
     */
    public List<ClubJoinRequest> getPendingJoinRequests(Long clubId, String adminUserId) {
        if (!isClubAdministrator(clubId, adminUserId)) {
            throw new SecurityException("동아리 관리자만 가입 요청을 조회할 수 있습니다.");
        }
        
        Club club = getClub(clubId);
        return joinRequestRepository.findByClubAndStatusOrderByCreatedAtDesc(club, ClubStatus.PENDING);
    }
    
    /**
     * 사용자의 가입 요청 목록 조회
     */
    public List<ClubJoinRequest> getUserJoinRequests(String userId) {
        User user = userRepository.findByUserId(userId);
        return joinRequestRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * 가입 요청 승인
     */
    @Transactional
    public ClubMember approveJoinRequest(Long requestId, String adminUserId) {
        ClubJoinRequest joinRequest = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("가입 요청을 찾을 수 없습니다."));
        
        if (!isClubAdministrator(joinRequest.getClub().getId(), adminUserId)) {
            throw new SecurityException("동아리 관리자만 가입 요청을 처리할 수 있습니다.");
        }
        
        if (joinRequest.getStatus() != ClubStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }
        
        // 이미 멤버인지 다시 한번 확인 (동시성 문제 방지)
        if (memberRepository.findByClubIdAndUserUserId(
                joinRequest.getClub().getId(), 
                joinRequest.getUser().getUserId()).isPresent()) {
            throw new IllegalStateException("이미 해당 동아리의 멤버입니다.");
        }
        
        // 가입 요청 승인 처리
        joinRequest.approve();
        joinRequestRepository.save(joinRequest);
        
        // 멤버로 등록
        ClubMember newMember = ClubMember.builder()
                .club(joinRequest.getClub())
                .user(joinRequest.getUser())
                .role(ClubRole.ROLE_MEMBER)
                .status(ClubStatus.ACTIVE)
                .build();
        
        return memberRepository.save(newMember);
    }
    
    /**
     * 가입 요청 거절
     */
    @Transactional
    public ClubJoinRequest rejectJoinRequest(Long requestId, String adminUserId, String rejectionReason) {
        ClubJoinRequest joinRequest = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("가입 요청을 찾을 수 없습니다."));
        
        if (!isClubAdministrator(joinRequest.getClub().getId(), adminUserId)) {
            throw new SecurityException("동아리 관리자만 가입 요청을 처리할 수 있습니다.");
        }
        
        if (joinRequest.getStatus() != ClubStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }
        
        // 가입 요청 거절 처리
        joinRequest.reject(rejectionReason);
        return joinRequestRepository.save(joinRequest);
    }
    
    /**
     * 가입 요청 취소 (사용자가 직접)
     */
    @Transactional
    public void cancelJoinRequest(Long requestId, String userId) {
        ClubJoinRequest joinRequest = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("가입 요청을 찾을 수 없습니다."));
        
        if (!joinRequest.getUser().getUserId().equals(userId)) {
            throw new SecurityException("본인의 가입 요청만 취소할 수 있습니다.");
        }
        
        if (joinRequest.getStatus() != ClubStatus.PENDING) {
            throw new IllegalStateException("대기중인 요청만 취소할 수 있습니다.");
        }
        
        joinRequestRepository.delete(joinRequest);
    }
    
    /**
     * 동아리의 대기중인 가입 요청 개수 조회
     */
    public Long getPendingJoinRequestCount(Long clubId) {
        Club club = getClub(clubId);
        return joinRequestRepository.countByClubAndStatus(club, ClubStatus.PENDING);
    }
    
    /**
     * 동아리 상세 정보 조회 (관리자용)
     */
    public ClubDetailResponse getClubDetail(Long clubId, String userId) {
        if (!isClubAdministrator(clubId, userId)) {
            throw new SecurityException("동아리 관리자만 상세 정보를 조회할 수 있습니다.");
        }
        
        Club club = getClub(clubId);
        
        // 기본 통계 정보
        int memberCount = memberRepository.findAllByClubId(clubId).size();
        int sessionCount = (int) attendanceSessionRepository.countByClubId(clubId);
        
        // 기본 응답 객체 생성
        ClubDetailResponse response = ClubDetailResponse.fromEntity(club, memberCount, sessionCount);
        
        // 상세 정보 추가
        response.setMembers(getClubMemberDetails(clubId));
        response.setAttendanceSessions(getAttendanceSessionSummaries(clubId));
        
        return response;
    }
    
    /**
     * 동아리 멤버 상세 정보 조회
     */
    private List<ClubDetailResponse.ClubMemberDetailDto> getClubMemberDetails(Long clubId) {
        List<ClubMember> members = memberRepository.findAllByClubId(clubId);
        
        return members.stream()
                .map(member -> {
                    User user = member.getUser();
                    
                    // 해당 사용자의 출석률 계산
                    double attendanceRate = calculateUserAttendanceRate(user.getUserId(), clubId);
                    
                    return ClubDetailResponse.ClubMemberDetailDto.builder()
                            .userId(user.getUserId())
                            .name(user.getName())
                            .nickname(user.getNickname())
                            .major(user.getMajor())
                            .school(user.getSchool())
                            .role(member.getRole().name())
                            .joinedAt(member.getJoinedAt())
                            .attendanceRate(attendanceRate)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 출석 세션 요약 정보 조회
     */
    private List<ClubDetailResponse.AttendanceSessionSummaryDto> getAttendanceSessionSummaries(Long clubId) {
        List<AttendanceSession> sessions = attendanceSessionRepository.findByClubIdWithAttendanceDetails(clubId);
        int totalMembers = memberRepository.findAllByClubId(clubId).size();
        
        return sessions.stream()
                .map(session -> {
                    List<Attendance> attendanceList = session.getAttendanceList();
                    int attendedMembers = attendanceList.size();
                    double sessionAttendanceRate = totalMembers > 0 ? 
                        (double) attendedMembers / totalMembers * 100 : 0.0;
                    
                    // 출석 상세 정보
                    List<ClubDetailResponse.AttendanceDetailDto> attendanceDetails = 
                        attendanceList.stream()
                            .map(attendance -> ClubDetailResponse.AttendanceDetailDto.builder()
                                    .userId(attendance.getUser().getUserId())
                                    .userName(attendance.getUser().getName())
                                    .attendanceTime(attendance.getAttendanceTime())
                                    .status(attendance.getStatus().name())
                                    .build())
                            .collect(Collectors.toList());
                    
                    // 세션 날짜와 시간을 결합
                    LocalDateTime sessionDateTime = LocalDateTime.of(
                        session.getSessionDate(), 
                        session.getStartTime()
                    );
                    
                    return ClubDetailResponse.AttendanceSessionSummaryDto.builder()
                            .sessionId(session.getId())
                            .sessionName(session.getSessionName())
                            .place(session.getPlace())
                            .sessionDateTime(sessionDateTime)
                            .totalMembers(totalMembers)
                            .attendedMembers(attendedMembers)
                            .attendanceRate(Math.round(sessionAttendanceRate * 100.0) / 100.0)
                            .attendanceDetails(attendanceDetails)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자의 동아리 출석률 계산
     */
    private double calculateUserAttendanceRate(String userId, Long clubId) {
        try {
            // 해당 사용자가 참석한 세션 수 (PRESENT 상태만)
            long attendedSessions = attendanceRepository.countAttendedSessionsByUserAndClub(
                userId, clubId, AttendanceStatus.PRESENT);
            
            // 해당 사용자가 가입한 이후의 총 세션 수
            long totalSessions = attendanceRepository.countTotalSessionsForUser(userId, clubId);
            
            if (totalSessions == 0) {
                return 0.0;
            }
            
            double rate = (double) attendedSessions / totalSessions * 100;
            return Math.round(rate * 100.0) / 100.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
