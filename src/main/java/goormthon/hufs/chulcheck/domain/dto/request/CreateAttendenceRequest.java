package goormthon.hufs.chulcheck.domain.dto.request;

import lombok.Data;

@Data
public class CreateAttendenceRequest {
    private Long sessionId;
    private String code;
}
