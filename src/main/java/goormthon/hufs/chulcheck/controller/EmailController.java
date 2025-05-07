package goormthon.hufs.chulcheck.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import goormthon.hufs.chulcheck.service.EmailService;
import lombok.RequiredArgsConstructor;

@RestController("/api/v1/email")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmailController {

	@Autowired
	private EmailService emailService;

	@PostMapping("/save/{email}")
	public ResponseEntity<String> saveEmail(@PathVariable String email) {
		emailService.saveEmail(email);
		return ResponseEntity.ok("email saving success!!");
	}
}
