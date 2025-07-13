package goormthon.hufs.chulcheck.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.request.AdditionalInfoRequest;
import goormthon.hufs.chulcheck.domain.dto.request.UpdateUserInfoRequest;
import goormthon.hufs.chulcheck.domain.dto.response.ApiResponse;
import goormthon.hufs.chulcheck.domain.dto.response.UserWithBadgesResponse;
import goormthon.hufs.chulcheck.service.user.UserManager;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

	@Autowired
	private final UserManager userManager;

	@GetMapping("/hello")
	public ResponseEntity<String> hello(Authentication authentication) {
		return ResponseEntity.ok("Hello World");
	}
	
	@GetMapping("/me")
	@Operation(description = "내 정보 조회 API (뱃지 정보 포함)")
	public ResponseEntity<ApiResponse<UserWithBadgesResponse>> getMyInfo(Authentication authentication) {
		CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
		
		UserWithBadgesResponse userInfo = userManager.getUserWithBadges(customOAuth2User);
		
		ApiResponse<UserWithBadgesResponse> response = ApiResponse.<UserWithBadgesResponse>builder()
			.status(200)
			.message("사용자 정보 조회 성공")
			.data(userInfo)
			.build();
			
		return ResponseEntity.ok(response);
	}
	
	@PatchMapping("/me")
	@Operation(description = "내 정보 수정 API")
	public ResponseEntity<ApiResponse<Void>> updateMyInfo(Authentication authentication,
			@RequestBody UpdateUserInfoRequest request) {
		CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
		
		userManager.updateUserInfo(customOAuth2User, request);
		
		ApiResponse<Void> response = ApiResponse.<Void>builder()
			.status(200)
			.message("사용자 정보 수정 성공")
			.data(null)
			.build();
			
		return ResponseEntity.ok(response);
	}
	
	@DeleteMapping("/me")
	@Operation(description = "회원 탈퇴 API")
	public ResponseEntity<ApiResponse<Void>> deleteMyAccount(Authentication authentication) {
		CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
		
		userManager.deleteUser(customOAuth2User);
		
		ApiResponse<Void> response = ApiResponse.<Void>builder()
			.status(200)
			.message("회원 탈퇴 완료")
			.data(null)
			.build();
			
		return ResponseEntity.ok(response);
	}

	@PutMapping("/additional-info")
	@Operation(description = "추가 정보를 가져오는 API")
	public ResponseEntity<?> additionalInfo(Authentication authentication,
		@RequestBody AdditionalInfoRequest additionalInfoRequest) {
		CustomOAuth2User customOAuth2User = (CustomOAuth2User)authentication.getPrincipal();

		userManager.putAdditionalInfo(customOAuth2User, additionalInfoRequest);

		ApiResponse<Void> response = ApiResponse.<Void>builder()
			.status(200)
			.message("추가 정보 저장 성공!")
			.data(null)
			.build();
		return ResponseEntity.ok(response);
	}
}
