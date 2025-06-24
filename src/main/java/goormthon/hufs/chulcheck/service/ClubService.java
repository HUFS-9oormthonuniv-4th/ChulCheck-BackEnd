package goormthon.hufs.chulcheck.service;

import goormthon.hufs.chulcheck.domain.dto.request.CreateClubRequest;
import goormthon.hufs.chulcheck.domain.dto.request.ManageClubMemberRequest;
import goormthon.hufs.chulcheck.domain.dto.request.UpdateClubRequest;
import goormthon.hufs.chulcheck.domain.dto.response.GetClubInfoResponse;
import goormthon.hufs.chulcheck.domain.entity.Club;
import goormthon.hufs.chulcheck.domain.entity.ClubMember;
import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.domain.enums.ClubRole;
import goormthon.hufs.chulcheck.domain.enums.ClubStatus;
import goormthon.hufs.chulcheck.repository.ClubRepository;
import goormthon.hufs.chulcheck.repository.ClubMemberRepository;
import goormthon.hufs.chulcheck.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import goormthon.hufs.chulcheck.domain.dto.response.GetClubInfoResponse.ClubInfo;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {
    private final ClubRepository clubRepository;
    private final ClubMemberRepository memberRepository;
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
}
