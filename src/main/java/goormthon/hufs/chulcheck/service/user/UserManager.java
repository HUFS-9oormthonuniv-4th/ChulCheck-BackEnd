package goormthon.hufs.chulcheck.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.request.AdditionalInfoRequest;
import goormthon.hufs.chulcheck.domain.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserManager {
	@Autowired
	private final UserService userService;

	public void putAdditionalInfo(CustomOAuth2User customOAuth2User, AdditionalInfoRequest additionalInfoRequest) {
		User user = userService.findByUserId(customOAuth2User);
		user.setName(additionalInfoRequest.getName());
		user.setSchool(additionalInfoRequest.getSchool());
		user.setMajor(additionalInfoRequest.getMajor());
		user.setStudentNum(additionalInfoRequest.getStudentNum());
		userService.save(user);
	}
}
