package goormthon.hufs.chulcheck.controller;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.request.CreateClubRequest;
import goormthon.hufs.chulcheck.domain.dto.request.UpdateClubRequest;
import goormthon.hufs.chulcheck.domain.dto.response.ClubMemberResponse;
import goormthon.hufs.chulcheck.domain.dto.response.ClubResponse;
import goormthon.hufs.chulcheck.domain.dto.response.GetClubInfoResponse;
import goormthon.hufs.chulcheck.domain.entity.Club;
import goormthon.hufs.chulcheck.domain.entity.ClubMember;
import goormthon.hufs.chulcheck.service.ClubService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/clubs")
@RestController
@RequiredArgsConstructor
public class ClubController {
    private final ClubService clubService;

    @GetMapping
    public List<GetClubInfoResponse> getAllClubs(Authentication authentication) {
        String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();
        return clubService.getClubsByUserId(userId);
    }

    @PostMapping
    public ResponseEntity<?> createClub(@RequestBody CreateClubRequest request, Authentication authentication) {
        try {
            String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();
            request.setOwnerId(userId);
            Club createdClub = clubService.createClub(request);
            ClubResponse response = ClubResponse.fromEntity(createdClub);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "동아리 생성 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{clubId}/administrators")
    public ResponseEntity<?> getClubAdministrators(@PathVariable Long clubId) {
        try {
            List<ClubMember> administrators = clubService.getAdministrators(clubId);
            List<ClubMemberResponse> responses = ClubMemberResponse.fromEntityList(administrators);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "관리자 목록 조회 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/{clubId}/administrators")
    public ResponseEntity<?> addClubAdministrator(
            @PathVariable Long clubId, 
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String currentUserId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();
        String newAdminUserId = request.get("userId");

        try {
            ClubMember administrator = clubService.addAdministrator(clubId, newAdminUserId, currentUserId);
            ClubMemberResponse response = ClubMemberResponse.fromEntity(administrator);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "관리자 추가 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/{clubId}")
    public ResponseEntity<?> updateClub(
            @PathVariable Long clubId,
            @RequestBody UpdateClubRequest request,
            Authentication authentication) {
        String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();

        try {
            Club updatedClub = clubService.updateClub(clubId, request, userId);
            ClubResponse response = ClubResponse.fromEntity(updatedClub);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "동아리 수정 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/{clubId}")
    public ResponseEntity<?> deleteClub(
            @PathVariable Long clubId,
            Authentication authentication) {
        String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();

        try {
            clubService.deleteClub(clubId, userId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "동아리 삭제 중 오류가 발생했습니다."));
        }
    }
}
