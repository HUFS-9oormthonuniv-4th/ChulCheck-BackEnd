package goormthon.hufs.chulcheck.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserInfoRequest {
    private String nickname;
    private String image;
    private String name;
    private String school;
    private String major;
    private String studentNum;
}