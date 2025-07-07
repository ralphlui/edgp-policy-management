package sg.edu.nus.iss.edgp.policy.management.exception;

public class PolicyServiceException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PolicyServiceException(String message) {
		super(message);
	}

	public PolicyServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
