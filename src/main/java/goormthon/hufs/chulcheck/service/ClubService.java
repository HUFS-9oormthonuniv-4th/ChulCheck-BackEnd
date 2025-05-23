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
        User owner = userRepository.findByUserId(req.getOwnerId());
        Club club = Club.builder()
            .name(req.getName())
            .representativeAlias(req.getRepresentativeAlias())
            .memberAlias(req.getMemberAlias())
            .description(req.getDescription())
            .owner(owner)
            .build();
        Club saved = clubRepository.save(club);

        ClubMember ownerMember = ClubMember.builder()
            .club(saved)
            .user(owner)
            .role(ClubRole.ROLE_MANAGER)
            .status(ClubStatus.ACTIVE)
            .build();
        memberRepository.save(ownerMember);
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
    public Club updateClub(Long clubId, UpdateClubRequest req) {
        Club club = getClub(clubId);
        club.setName(req.getName());
        club.setRepresentativeAlias(req.getRepresentativeAlias());
        club.setMemberAlias(req.getMemberAlias());
        club.setDescription(req.getDescription());
        return clubRepository.save(club);
    }

    @Transactional
    public void deleteClub(Long clubId) {
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
    public void removeMember(Long clubId, String userId) {
        ClubMember member = memberRepository.findByClubIdAndUserUserId(clubId, userId)
            .orElseThrow(() -> new EntityNotFoundException("해당 User의 모임 가입정보를 찾을 수 없습니다: " + userId));
        memberRepository.delete(member);
    }

    public List<ClubMember> getMembers(Long clubId) {
        getClub(clubId);
        return memberRepository.findAllByClubId(clubId);
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
}
