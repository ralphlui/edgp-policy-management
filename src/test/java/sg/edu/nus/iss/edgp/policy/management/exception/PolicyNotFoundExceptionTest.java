package sg.edu.nus.iss.edgp.policy.management.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PolicyNotFoundExceptionTest {

	@Test
	public void testConstructor_WithMessage() {
		String errorMessage = "Policy not found";
		PolicyNotFoundException exception = new PolicyNotFoundException(errorMessage);

		assertEquals(errorMessage, exception.getMessage());
		assertNull(exception.getCause());
	}

	@Test
	public void testConstructor_WithMessageAndCause() {
		String errorMessage = "Policy lookup failed";
		Throwable cause = new RuntimeException("Database error");
		PolicyNotFoundException exception = new PolicyNotFoundException(errorMessage, cause);

		assertEquals(errorMessage, exception.getMessage());
		assertEquals(cause, exception.getCause());
	}

	@Test
	public void testExceptionThrown() {
		String errorMessage = "Policy not available";

		Exception exception = assertThrows(PolicyNotFoundException.class, () -> {
			throw new PolicyNotFoundException(errorMessage);
		});

		assertEquals(errorMessage, exception.getMessage());
	}
}
