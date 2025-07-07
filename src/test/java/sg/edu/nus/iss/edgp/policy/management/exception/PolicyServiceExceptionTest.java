package sg.edu.nus.iss.edgp.policy.management.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PolicyServiceExceptionTest {

	@Test
	public void testConstructor_WithMessage() {
		String message = "Service error occurred";
		PolicyServiceException exception = new PolicyServiceException(message);

		assertEquals(message, exception.getMessage());
		assertNull(exception.getCause());
	}

	@Test
	public void testConstructor_WithMessageAndCause() {
		String message = "Error while processing policy";
		Throwable cause = new RuntimeException("Underlying DB failure");

		PolicyServiceException exception = new PolicyServiceException(message, cause);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
	}

	@Test
	public void testExceptionThrown() {
		String message = "Service crash";

		Exception exception = assertThrows(PolicyServiceException.class, () -> {
			throw new PolicyServiceException(message);
		});

		assertEquals(message, exception.getMessage());
	}
}
