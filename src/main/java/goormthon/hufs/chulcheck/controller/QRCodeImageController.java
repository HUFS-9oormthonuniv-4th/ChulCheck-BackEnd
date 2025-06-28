package goormthon.hufs.chulcheck.controller;

import goormthon.hufs.chulcheck.domain.entity.AttendanceSession;
import goormthon.hufs.chulcheck.service.AttendanceSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/api/v1/qr-images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "QR Code Image", description = "QR 코드 이미지 직접 제공 API")
@CrossOrigin(origins = "*")
public class QRCodeImageController {
    
    private final AttendanceSessionService attendanceSessionService;
    
    @GetMapping("/{sessionId}")
    @Operation(summary = "QR 코드 이미지 직접 다운로드", description = "출석 세션의 QR 코드를 PNG 이미지로 직접 제공합니다.")
    public ResponseEntity<byte[]> getQRCodeImage(@PathVariable Long sessionId) {
        try {
            AttendanceSession session = attendanceSessionService.getSession(sessionId);
            
            if (session.getQrCodeImage() == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Base64 디코딩
            byte[] imageBytes = Base64.getDecoder().decode(session.getQrCodeImage());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(imageBytes.length);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                    "inline; filename=\"qr-code-" + sessionId + ".png\"");
            
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("QR 코드 이미지 제공 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
