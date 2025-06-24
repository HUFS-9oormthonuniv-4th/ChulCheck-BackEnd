package goormthon.hufs.chulcheck.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.request.AdditionalInfoRequest;
import goormthon.hufs.chulcheck.domain.dto.response.ApiResponse;
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
