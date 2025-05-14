package goormthon.hufs.chulcheck.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import goormthon.hufs.chulcheck.domain.dto.CustomOAuth2User;
import goormthon.hufs.chulcheck.domain.dto.OAuth2UserDTO;
import goormthon.hufs.chulcheck.domain.dto.request.AdditionalInfoRequest;
import goormthon.hufs.chulcheck.domain.entity.User;
import goormthon.hufs.chulcheck.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();



	private void saveTestUserToRepository() {
		User user = User.builder()
			.userId("test_id")
			.nickname("test_nickname")
			.role("test_role")
			.image("test_image")
			.build();
		userRepository.save(user);
	}

	private void setAuthenticationContext() {
		OAuth2UserDTO userDTO = OAuth2UserDTO.builder()
			.userId("test_id")
			.role("test_role")
			.nickname("test_nickname")
			.image("test_image")
			.build();

		CustomOAuth2User principal = new CustomOAuth2User(userDTO);
		var auth = new TestingAuthenticationToken(principal, null, "ROLE_USER");
		SecurityContextHolder.createEmptyContext().setAuthentication(auth);
		SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	@Test
	void 추가정보저장_성공() throws Exception {
		// given
		saveTestUserToRepository();

		AdditionalInfoRequest request = AdditionalInfoRequest.builder()
			.major("test_major")
			.name("test_name")
			.school("test_school")
			.studentNum("test_student_num")
			.build();

		setAuthenticationContext();

		// when & then
		mockMvc.perform(
			put("/api/v1/user/additional-info")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andExpect(status().isOk());

		User test_user = userRepository.findByUserId("test_id");

		assertThat(test_user.getUserId()).isEqualTo("test_id");
		assertThat(test_user.getRole()).isEqualTo("test_role");
		assertThat(test_user.getImage()).isEqualTo("test_image");
		assertThat(test_user.getMajor()).isEqualTo("test_major");
		assertThat(test_user.getSchool()).isEqualTo("test_school");
		assertThat(test_user.getStudentNum()).isEqualTo("test_student_num");
		assertThat(test_user.getName()).isEqualTo("test_name");
		assertThat(test_user.getNickname()).isEqualTo("test_nickname");
	}
}