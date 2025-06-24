package goormthon.hufs.chulcheck.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

@Component
@Slf4j
public class QRCodeUtil {
    
    private static final int QR_CODE_SIZE = 300;
    
    /**
     * QR 코드를 생성하고 Base64 인코딩된 이미지 문자열로 반환
     */
    public String generateQRCodeImage(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            // BufferedImage를 Base64 문자열로 변환
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            return Base64.getEncoder().encodeToString(imageBytes);
            
        } catch (WriterException | IOException e) {
            log.error("QR 코드 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("QR 코드 생성에 실패했습니다.", e);
        }
    }
    
    /**
     * 출석 코드를 포함한 JSON 문자열 생성
     */
    public String createAttendanceQRData(Long sessionId, String attendanceCode) {
        return String.format("{\"sessionId\":%d,\"code\":\"%s\"}", sessionId, attendanceCode);
    }
}
