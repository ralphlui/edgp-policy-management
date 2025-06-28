package sg.edu.nus.iss.edgp.policy.management.dto;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationResult {
	private boolean isValid;
	private String message;
	private HttpStatus status;
}