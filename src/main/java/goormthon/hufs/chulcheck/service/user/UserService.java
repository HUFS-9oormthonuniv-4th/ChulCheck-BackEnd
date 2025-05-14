package goormthon.hufs.chulcheck.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	@Autowired
	private final UserRepository userRepository;

	public User findByUserId(CustomOAuth2User customOAuth2User) {
		return userRepository.findByUserId(customOAuth2User.getUserId());
	}

	public void save(User user) {
		userRepository.save(user);
	}
}
