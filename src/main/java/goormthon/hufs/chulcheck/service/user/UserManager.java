package goormthon.hufs.chulcheck.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.request.AdditionalInfoRequest;
import goormthon.hufs.chulcheck.domain.dto.request.UpdateUserInfoRequest;
import goormthon.hufs.chulcheck.domain.dto.response.BadgeResponse;
import goormthon.hufs.chulcheck.domain.dto.response.UserWithBadgesResponse;
import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.domain.entity.UserBadge;
import goormthon.hufs.chulcheck.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManager {
	@Autowired
	private final UserService userService;
	
	@Autowired
	private final UserBadgeRepository userBadgeRepository;

	public void putAdditionalInfo(CustomOAuth2User customOAuth2User, AdditionalInfoRequest additionalInfoRequest) {
		User user = userService.findByUserId(customOAuth2User);
		user.setName(additionalInfoRequest.getName());
		user.setSchool(additionalInfoRequest.getSchool());
		user.setMajor(additionalInfoRequest.getMajor());
		user.setStudentNum(additionalInfoRequest.getStudentNum());
		userService.save(user);
	}
	
	public UserWithBadgesResponse getUserWithBadges(CustomOAuth2User customOAuth2User) {
		User user = userService.findByUserId(customOAuth2User);
		List<UserBadge> userBadges = userBadgeRepository.findByUserWithBadge(user);
		
		List<BadgeResponse> badgeResponses = userBadges.stream()
				.map(userBadge -> BadgeResponse.fromBadgeAndUserBadge(
						userBadge.getBadge(), 
						userBadge.getCreatedAt()))
				.toList();
		
		return UserWithBadgesResponse.fromUserAndBadges(user, badgeResponses);
	}
	
	public void updateUserInfo(CustomOAuth2User customOAuth2User, UpdateUserInfoRequest request) {
		User user = userService.findByUserId(customOAuth2User);
		
		if (request.getNickname() != null) {
			user.setNickname(request.getNickname());
		}
		if (request.getImage() != null) {
			user.setImage(request.getImage());
		}
		if (request.getName() != null) {
			user.setName(request.getName());
		}
		if (request.getSchool() != null) {
			user.setSchool(request.getSchool());
		}
		if (request.getMajor() != null) {
			user.setMajor(request.getMajor());
		}
		if (request.getStudentNum() != null) {
			user.setStudentNum(request.getStudentNum());
		}
		
		userService.save(user);
	}
	
	public void deleteUser(CustomOAuth2User customOAuth2User) {
		User user = userService.findByUserId(customOAuth2User);
		user.setIsActive(false);
		userService.save(user);
	}
}
