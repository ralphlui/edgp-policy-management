package sg.edu.nus.iss.edgp.policy.management.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sg.edu.nus.iss.edgp.policy.management.connector.OrganizationAPICall;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyRequest;
import sg.edu.nus.iss.edgp.policy.management.dto.ValidationResult;
import sg.edu.nus.iss.edgp.policy.management.entity.Policy;
import sg.edu.nus.iss.edgp.policy.management.service.impl.JwtService;
import sg.edu.nus.iss.edgp.policy.management.service.impl.PolicyService;
import sg.edu.nus.iss.edgp.policy.management.strategy.impl.PolicyValidationStrategy;
import sg.edu.nus.iss.edgp.policy.management.utility.JSONReader;

@ExtendWith(MockitoExtension.class)
public class PolicyValidationStrategyTest {

	@InjectMocks
	private PolicyValidationStrategy validationStrategy;

	@Mock
	private PolicyService policyService;

	@Mock
	private OrganizationAPICall orgAPICall;

	@Mock
	private JSONReader jsonReader;

	@Mock
	private JwtService jwtService;

	private static final String AUTH_HEADER = "Bearer dummy.jwt.token";
	private String userOrgId = "org123";

	@BeforeEach
	public void setup() {
		validationStrategy = new PolicyValidationStrategy(policyService, orgAPICall, jsonReader);
	}

	@Test
	public void testValidateCreation_MissingFields() {
		PolicyRequest request = new PolicyRequest(); // all nulls

		ValidationResult result = validationStrategy.validateCreation(request, AUTH_HEADER, userOrgId);

		assertFalse(result.isValid());
		assertEquals("Policy name is required", result.getMessage());
	}

	@Test
	public void testValidateCreation_DuplicatePolicy() {
		PolicyRequest request = new PolicyRequest();
		request.setPolicyName("TestPolicy");
		request.setDomainName("DomainA");

		when(policyService.findByPolicyName("TestPolicy")).thenReturn(new Policy());

		ValidationResult result = validationStrategy.validateCreation(request, AUTH_HEADER, userOrgId);

		assertFalse(result.isValid());
		assertEquals("Duplicate policy detected. Please enter a unique name.", result.getMessage());
	}

	@Test
	public void testValidateCreation_InactiveOrganization() throws Exception {
		PolicyRequest request = new PolicyRequest();
		request.setPolicyName("TestPolicy");
		request.setDomainName("DomainA");

		when(policyService.findByPolicyName("TestPolicy")).thenReturn(null);
		when(orgAPICall.validateActiveOrganization(any(), any()))
				.thenReturn("{\"success\":true,\"data\":{\"active\":false}}");

		JSONObject mockResponse = new JSONObject();
		mockResponse.put("success", true);
		JSONObject data = new JSONObject();
		data.put("active", false);
		mockResponse.put("data", data);

		when(jsonReader.getSuccessFromResponse(any())).thenReturn(true);
		when(jsonReader.getDataFromResponse(any())).thenReturn(data);

		ValidationResult result = validationStrategy.validateCreation(request, AUTH_HEADER, userOrgId);

		assertFalse(result.isValid());
		assertEquals("Invalid organization. Unable to create policy.", result.getMessage());
	}

	@Test
	public void testValidateCreation_ValidRequest() throws Exception {
		PolicyRequest request = new PolicyRequest();
		request.setPolicyName("TestPolicy");
		request.setDomainName("DomainA");

		when(policyService.findByPolicyName("TestPolicy")).thenReturn(null);
		when(orgAPICall.validateActiveOrganization(any(), any()))
				.thenReturn("{\"success\":true,\"data\":{\"active\":true}}");

		JSONObject mockResponse = new JSONObject();
		mockResponse.put("success", true);
	    JSONObject data = new JSONObject();
		data.put("active", true);
		mockResponse.put("data", data);

		when(jsonReader.getSuccessFromResponse(any())).thenReturn(true);
		when(jsonReader.getDataFromResponse(any())).thenReturn(data);

		ValidationResult result = validationStrategy.validateCreation(request, AUTH_HEADER, userOrgId);

		assertTrue(result.isValid());
	}

	@Test
	public void testValidateUpdating_MissingPolicyId() {
		PolicyRequest request = new PolicyRequest();
		request.setPolicyId("");

		ValidationResult result = validationStrategy.validateUpdating(request);

		assertFalse(result.isValid());
		assertEquals("Bad Request: Policy ID could not be blank.", result.getMessage());
	}

	@Test
	public void testValidateUpdating_InvalidPolicyId() {
		PolicyRequest request = new PolicyRequest();
		request.setPolicyId("invalid-id");

		when(policyService.findByPolicyId("invalid-id")).thenReturn(null);

		ValidationResult result = validationStrategy.validateUpdating(request);

		assertFalse(result.isValid());
		assertEquals("Invalid policy ID.", result.getMessage());
	}

	@Test
	public void testValidateUpdating_ValidPolicyId() {
		PolicyRequest request = new PolicyRequest();
		request.setPolicyId("valid-id");

		PolicyDTO policyDTO = new PolicyDTO();
		policyDTO.setPolicyId("valid-id");

		when(policyService.findByPolicyId("valid-id")).thenReturn(policyDTO);

		ValidationResult result = validationStrategy.validateUpdating(request);

		assertTrue(result.isValid());
	}
}
