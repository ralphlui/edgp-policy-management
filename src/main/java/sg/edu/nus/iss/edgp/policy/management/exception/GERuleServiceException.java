package sg.edu.nus.iss.edgp.policy.management.exception;

public class GERuleServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GERuleServiceException(String message) {
		super(message);
	}

	public GERuleServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
