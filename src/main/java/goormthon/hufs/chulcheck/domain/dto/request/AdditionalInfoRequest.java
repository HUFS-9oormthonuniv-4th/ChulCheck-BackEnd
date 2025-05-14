package goormthon.hufs.chulcheck.domain.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdditionalInfoRequest {
	private String name;
	private String school;
	private String major;
	private String studentNum;

	@Builder
	public AdditionalInfoRequest(String name, String school, String major, String studentNum) {
		this.name = name;
		this.school = school;
		this.major = major;
		this.studentNum = studentNum;
	}
}
