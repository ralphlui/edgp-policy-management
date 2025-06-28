package sg.edu.nus.iss.edgp.policy.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class APIResponse<T> {

	private Boolean success;
	private String message;
	private long totalRecord;
	private T data;

	public static <T> APIResponse<T> success(T data, String message) {
		return APIResponse.<T>builder().success(true).message(message).data(data).totalRecord(1).build();
	}

	public static <T> APIResponse<T> error(String message) {
		return APIResponse.<T>builder().success(false).message(message).totalRecord(0).build();
	}

}
