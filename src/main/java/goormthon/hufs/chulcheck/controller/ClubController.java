package goormthon.hufs.chulcheck.controller;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.request.CreateClubRequest;
import goormthon.hufs.chulcheck.domain.dto.request.UpdateClubRequest;
import goormthon.hufs.chulcheck.domain.dto.response.GetClubInfoResponse;
import goormthon.hufs.chulcheck.domain.entity.Club;
import goormthon.hufs.chulcheck.domain.entity.ClubMember;
import goormthon.hufs.chulcheck.service.ClubService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    public ResponseEntity<Club> createClub(@RequestBody CreateClubRequest request, Authentication authentication) {
        String userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();
        request.setOwnerId(userId);
        Club createdClub = clubService.createClub(request);
        return new ResponseEntity<>(createdClub, HttpStatus.CREATED);
    }

    @GetMapping("/{clubId}/administrators")
    public ResponseEntity<List<ClubMember>> getClubAdministrators(@PathVariable Long clubId) {
        List<ClubMember> administrators = clubService.getAdministrators(clubId);
        return ResponseEntity.ok(administrators);
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
            return ResponseEntity.ok(administrator);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
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
            return ResponseEntity.ok(updatedClub);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
