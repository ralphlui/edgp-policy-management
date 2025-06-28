package sg.edu.nus.iss.edgp.policy.management.exception;

public class PolicyNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PolicyNotFoundException(String message) {
		super(message);
	}
	
	public PolicyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
