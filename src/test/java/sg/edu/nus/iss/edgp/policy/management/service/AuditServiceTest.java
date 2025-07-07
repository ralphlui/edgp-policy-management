package sg.edu.nus.iss.edgp.policy.management.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import sg.edu.nus.iss.edgp.policy.management.aws.service.SQSPublishingService;
import sg.edu.nus.iss.edgp.policy.management.dto.AuditDTO;
import sg.edu.nus.iss.edgp.policy.management.enums.AuditResponseStatus;
import sg.edu.nus.iss.edgp.policy.management.service.impl.AuditService;
import sg.edu.nus.iss.edgp.policy.management.service.impl.JwtService;

public class AuditServiceTest {

	@InjectMocks
	private AuditService auditService;

	@Mock
	private SQSPublishingService sqsPublishingService;

	@Mock
	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(auditService, "activityTypePrefix", "PREFIX_");
	}

	@Test
	void testCreateAuditDTO() {
		AuditDTO dto = auditService.createAuditDTO("LOGIN", "/login", "POST");
		assert dto.getActivityType().equals("PREFIX_LOGIN");
		assert dto.getRequestHTTPVerb().equals("POST");
		assert dto.getRequestActionEndpoint().equals("/login");
	}

	@Test
	void testLogAuditSuccess() {
		AuditDTO dto = new AuditDTO();
		String token = "Bearer valid.jwt.token";

		when(jwtService.extractUserNameFromToken("valid.jwt.token")).thenReturn("testUser");
		when(jwtService.extractUserIdFromToken("valid.jwt.token")).thenReturn("123");

		auditService.logAudit(dto, 200, "Success message", token);

		assert dto.getStatusCode() == 200;
		assert dto.getResponseStatus() == AuditResponseStatus.SUCCESS;
		assert dto.getActivityDescription().equals("Success message");

		verify(sqsPublishingService).sendMessage(any(AuditDTO.class));
	}

	@Test
	void testLogAuditFailure() {
		AuditDTO dto = new AuditDTO();
		String token = "Bearer invalid.jwt.token";

		when(jwtService.extractUserNameFromToken("invalid.jwt.token")).thenReturn(null);
		when(jwtService.extractUserIdFromToken("invalid.jwt.token")).thenReturn(null);

		auditService.logAudit(dto, 400, "Error message", token);

		assert dto.getStatusCode() == 400;
		assert dto.getResponseStatus() == AuditResponseStatus.FAILED;
		assert dto.getActivityDescription().equals("Error message");

		verify(sqsPublishingService).sendMessage(any(AuditDTO.class));
	}

	@Test
	void testSendMessageWithEmptyAuthorizationHeader() {
		AuditDTO dto = new AuditDTO();

		auditService.sendMessage(dto, "");

		assert dto.getUsername().equals("Invalid Username");
		verify(sqsPublishingService).sendMessage(dto);
	}

	@Test
	void testSendMessageWithValidToken() {
		AuditDTO dto = new AuditDTO();
		String token = "Bearer valid.token.here";

		when(jwtService.extractUserNameFromToken("valid.token.here")).thenReturn("alice");
		when(jwtService.extractUserIdFromToken("valid.token.here")).thenReturn("101");

		auditService.sendMessage(dto, token);

		assert dto.getUsername().equals("alice");
		assert dto.getUserId().equals("101");
		verify(sqsPublishingService).sendMessage(dto);
	}

	@Test
	void testSendMessageHandlesException() {
		AuditDTO dto = new AuditDTO();
		String token = "Bearer valid.token";

		when(jwtService.extractUserNameFromToken("valid.token")).thenThrow(new RuntimeException("JWT Error"));

		auditService.sendMessage(dto, token);

		// Even on exception, sendMessage is not called with incomplete data
		verify(sqsPublishingService, times(0)).sendMessage(dto);
	}
}
