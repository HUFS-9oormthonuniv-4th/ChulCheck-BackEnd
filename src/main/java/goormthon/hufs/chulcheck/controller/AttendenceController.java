package goormthon.hufs.chulcheck.controller;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.request.CreateAttendenceRequest;
import goormthon.hufs.chulcheck.domain.dto.response.GetAttendanceResponse;
import goormthon.hufs.chulcheck.service.AttendanceService;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AttendenceController {
    private final AttendanceService attendanceService;

    @PostMapping("/attendence")
    public void attendence(Authentication authentication,
                           @RequestBody CreateAttendenceRequest request) {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User)authentication.getPrincipal();
        String userId = customOAuth2User.getUserId();
        attendanceService.createAttendance(userId, request.getSessionId(), request.getCode());
    }

    @GetMapping("/attendence")
    public List<GetAttendanceResponse> getAllAttendences(Authentication authentication,
                                                     @RequestParam Long clubId) {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User)authentication.getPrincipal();
        String userId = customOAuth2User.getUserId();
        return attendanceService.getAllAttendancesByUserAndClub(userId, clubId);
    }
}
