package goormthon.hufs.chulcheck.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import goormthon.hufs.chulcheck.domain.entity.Email;
import goormthon.hufs.chulcheck.repository.EmailRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
	@Autowired
	private EmailRepository emailRepository;
	
	@Value("${aws.ses.access-key}")
	private String accessKey;
	
	@Value("${aws.ses.secret-key}")
	private String secretKey;
	
	@Value("${aws.ses.region}")
	private String region;
	
	@Value("${aws.ses.from-email}")
	private String fromEmail;
	
	private SesClient sesClient;
	
	@PostConstruct
	public void initSesClient() {
		try {
			AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
			
			this.sesClient = SesClient.builder()
				.region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
				.build();
				
			log.info("AWS SES client initialized successfully for region: {}", region);
			
		} catch (Exception e) {
			log.error("Failed to initialize AWS SES client", e);
			throw new RuntimeException("AWS SES 클라이언트 초기화에 실패했습니다: " + e.getMessage());
		}
	}
	
	@PreDestroy
	public void closeSesClient() {
		if (sesClient != null) {
			sesClient.close();
			log.info("AWS SES client closed successfully");
		}
	}

	public void saveEmail(String email) {
		Email newEmail = Email.builder()
			.email(email)
			.build();
		emailRepository.save(newEmail);
	}
	
	public void sendPasswordResetEmail(String toEmail, String token) {
		try {
			String subject = "ChulCheck - 비밀번호 재설정";
			String bodyText = 
				"비밀번호 재설정을 요청하셨습니다.\n\n" +
				"다음 토큰을 사용하여 비밀번호를 재설정해주세요:\n" +
				token + "\n\n" +
				"이 토큰은 1시간 후에 만료됩니다.\n" +
				"만약 비밀번호 재설정을 요청하지 않으셨다면 이 메일을 무시해주세요.";
			
			String bodyHtml = 
				"<html>" +
				"<head></head>" +
				"<body>" +
				"<h2>ChulCheck - 비밀번호 재설정</h2>" +
				"<p>비밀번호 재설정을 요청하셨습니다.</p>" +
				"<p>다음 토큰을 사용하여 비밀번호를 재설정해주세요:</p>" +
				"<div style='background-color: #f0f0f0; padding: 10px; margin: 10px 0; font-family: monospace; font-size: 14px;'>" +
				token +
				"</div>" +
				"<p><strong>이 토큰은 1시간 후에 만료됩니다.</strong></p>" +
				"<p style='color: #666; font-size: 12px;'>만약 비밀번호 재설정을 요청하지 않으셨다면 이 메일을 무시해주세요.</p>" +
				"</body>" +
				"</html>";
			
			SendEmailRequest request = SendEmailRequest.builder()
				.source(fromEmail)
				.destination(Destination.builder()
					.toAddresses(toEmail)
					.build())
				.message(Message.builder()
					.subject(Content.builder()
						.charset("UTF-8")
						.data(subject)
						.build())
					.body(Body.builder()
						.text(Content.builder()
							.charset("UTF-8")
							.data(bodyText)
							.build())
						.html(Content.builder()
							.charset("UTF-8")
							.data(bodyHtml)
							.build())
						.build())
					.build())
				.build();
			
			SendEmailResponse response = sesClient.sendEmail(request);
			log.info("Password reset email sent successfully to: {} with messageId: {}", 
				toEmail, response.messageId());
				
		} catch (SesException e) {
			log.error("AWS SES error sending email to: {} - Error: {}", toEmail, e.awsErrorDetails().errorMessage(), e);
			throw new RuntimeException("메일 발송에 실패했습니다: " + e.awsErrorDetails().errorMessage());
		} catch (Exception e) {
			log.error("Failed to send password reset email to: {}", toEmail, e);
			throw new RuntimeException("메일 발송에 실패했습니다: " + e.getMessage());
		}
	}
}
