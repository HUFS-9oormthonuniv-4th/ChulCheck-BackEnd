package goormthon.hufs.chulcheck.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import goormthon.hufs.chulcheck.domain.entity.Email;
import goormthon.hufs.chulcheck.repository.EmailRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
	@Autowired
	private EmailRepository emailRepository;

	public void saveEmail(String email) {
		Email newEmail = Email.builder()
			.email(email)
			.build();
		emailRepository.save(newEmail);
	}
}
