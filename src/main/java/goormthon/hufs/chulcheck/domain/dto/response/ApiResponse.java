package goormthon.hufs.chulcheck.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {
	private int status;
	private String message;
	private T data;
}
