package sg.edu.nus.iss.edgp.policy.management.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.nus.iss.edgp.policy.management.dto.PolicyDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyRequest;
import sg.edu.nus.iss.edgp.policy.management.dto.SearchRequest;
import sg.edu.nus.iss.edgp.policy.management.dto.ValidationResult;
import sg.edu.nus.iss.edgp.policy.management.exception.PolicyServiceException;
import sg.edu.nus.iss.edgp.policy.management.service.impl.AuditService;
import sg.edu.nus.iss.edgp.policy.management.service.impl.JwtService;
import sg.edu.nus.iss.edgp.policy.management.service.impl.PolicyService;
import sg.edu.nus.iss.edgp.policy.management.strategy.impl.PolicyValidationStrategy;

@WebMvcTest(PolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
class PolicyControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PolicyService policyService;

	@MockitoBean
	private AuditService auditService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private PolicyValidationStrategy policyValidationStrategy;

	@Autowired
	private ObjectMapper objectMapper;

	private final String authorizationHeader = "Bearer dummy.jwt.token";
	private final String token = "dummy.jwt.token";
	private final String policyId = "POL123";
	private final SearchRequest searchRequest = new SearchRequest();

	@Test
	void testCreatePolicy_Success() throws Exception {
		// Prepare mock request and response
		PolicyRequest policyRequest = new PolicyRequest(); // Populate with valid data
		PolicyDTO policyDTO = new PolicyDTO(); // Populate with expected response
		String jwtToken = "mock-jwt-token";
		String userId = "user-123";
		String authHeader = "Bearer " + jwtToken;

		ValidationResult validationResult = new ValidationResult();
		validationResult.setValid(true);

		when(jwtService.extractSubject(jwtToken)).thenReturn(userId);
		when(policyValidationStrategy.validateCreation(any(), eq(authHeader), any())).thenReturn(validationResult);
		when(policyService.createPolicy(any(), eq(userId))).thenReturn(policyDTO);

		// Perform the request
		mockMvc.perform(post("/api/policy").header("Authorization", authHeader).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(policyRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Success! The new policy has been added."))
				.andExpect(jsonPath("$.data").exists());

	}

	@Test
	void testCreatePolicy_ValidationFails() throws Exception {
		PolicyRequest policyRequest = new PolicyRequest(); // Possibly invalid data
		String jwtToken = "mock-jwt-token";
		String authHeader = "Bearer " + jwtToken;

		ValidationResult validationResult = new ValidationResult();
		validationResult.setValid(false);
		validationResult.setMessage("Invalid input");
		validationResult.setStatus(HttpStatus.BAD_REQUEST);

		when(jwtService.extractSubject(jwtToken)).thenReturn("user-123");
		when(policyValidationStrategy.validateCreation(any(), eq(authHeader), any())).thenReturn(validationResult);

		mockMvc.perform(post("/api/policy").header("Authorization", authHeader).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(policyRequest))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Invalid input"));
	}

	@Test
	void testCreatePolicy_InternalServerError() throws Exception {
		PolicyRequest policyRequest = new PolicyRequest();
		String jwtToken = "mock-jwt-token";
		String authHeader = "Bearer " + jwtToken;

		when(jwtService.extractSubject(jwtToken)).thenReturn("user-123");
		when(policyValidationStrategy.validateCreation(any(), eq(authHeader), any()))
				.thenThrow(new PolicyServiceException("Unexpected error"));

		mockMvc.perform(post("/api/policy").header("Authorization", authHeader).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(policyRequest))).andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.message").exists());

	}

	@Test
	void testRetrievePolicyList_WithPagination_Success() throws Exception {
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setPage(1);
		searchRequest.setSize(10);
		searchRequest.setIsPublished(true);

		List<PolicyDTO> policyList = List.of(new PolicyDTO());
		Map<Long, List<PolicyDTO>> resultMap = Map.of(1L, policyList);
		ValidationResult validationResult = new ValidationResult();
		validationResult.setValid(true);

		when(jwtService.extractOrgIdFromToken("dummy.jwt.token")).thenReturn("org123");
		when(policyValidationStrategy.isUserOrganizationActive("org123", authorizationHeader))
				.thenReturn(validationResult);

		when(policyService.retrievePaginatedPolicyList(any(Pageable.class), any(SearchRequest.class), eq("org123")))
				.thenReturn(resultMap);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/policy").param("page", "1").param("size", "10")
				.param("isPublished", "true").header("Authorization", authorizationHeader)).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.totalRecord").value(1));
	}

	@Test
	void testRetrievePolicyList_MissingOrgId_ReturnsBadRequest() throws Exception {

		ValidationResult validationResult = new ValidationResult();
		validationResult.setValid(false);
		validationResult.setMessage("org inactive");

		when(jwtService.extractOrgIdFromToken("dummy.jwt.token")).thenReturn("");
		when(policyValidationStrategy.isUserOrganizationActive(eq(""), eq(authorizationHeader)))
				.thenReturn(validationResult);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/policy").param("isPublished", "true").header("Authorization",
				authorizationHeader)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void testRetrievePolicyList_NoPolicyFound() throws Exception {

		ValidationResult validationResult = new ValidationResult();
		validationResult.setValid(true);

		when(jwtService.extractOrgIdFromToken("dummy.jwt.token")).thenReturn("org123");
		when(policyValidationStrategy.isUserOrganizationActive("org123", authorizationHeader))
				.thenReturn(validationResult);

		searchRequest.setIsPublished(true);
		searchRequest.setDomainName("Customer");
		when(policyService.retrieveAllPolicyList(any(SearchRequest.class), eq("org123"))).thenReturn(Map.of(0L, List.of()));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/policy").
				header("Authorization",authorizationHeader))
		        .andExpect(status().isOk())
		        .andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").isEmpty()).andDo(print());
	}
	

	@Test
	void testRetrievePolicyList_ServiceException_ReturnsInternalServerError() throws Exception {

		ValidationResult validationResult = new ValidationResult();
		validationResult.setValid(true);

		when(jwtService.extractOrgIdFromToken("dummy.jwt.token")).thenReturn("org123");
		when(policyValidationStrategy.isUserOrganizationActive("org123", authorizationHeader))
				.thenReturn(validationResult);

		searchRequest.setIsPublished(true);
		when(policyService.retrieveAllPolicyList(searchRequest, "org123"))
				.thenThrow(new PolicyServiceException("Unexpected error"));
		mockMvc.perform(MockMvcRequestBuilders.get("/api/policy").param("isPublished", "true").header("Authorization",
				authorizationHeader)).andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void testUpdatePolicy_Success() throws Exception {
		String policyId = "abc123";
		String userId = "user001";
		PolicyRequest request = new PolicyRequest();
		request.setPolicyName("Updated Policy");

		PolicyDTO updatedPolicy = new PolicyDTO();
		updatedPolicy.setPolicyId(policyId);
		updatedPolicy.setPolicyName("Updated Policy");

		ValidationResult validationResult = new ValidationResult();
		validationResult.setValid(true);
		validationResult.setStatus(HttpStatus.OK);

		when(jwtService.extractSubject("dummy.jwt.token")).thenReturn(userId);
		when(policyValidationStrategy.validateUpdating(any(PolicyRequest.class))).thenReturn(validationResult);
		when(policyService.updatePolicy(any(), eq(userId), eq(policyId))).thenReturn(updatedPolicy);

		mockMvc.perform(MockMvcRequestBuilders.put("/api/policy").header("Authorization", authorizationHeader)
				.header("X-Policy-Id", policyId).contentType(MediaType.APPLICATION_JSON).content("""
						    {
						      "policyName": "Updated Policy"
						    }
						""")).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.policyName").value("Updated Policy"));
	}

	@Test
	void testUpdatePolicy_ValidationFailure() throws Exception {
		String policyId = "abc123";
		String errorMessage = "Invalid policy data";

		ValidationResult validationResult = new ValidationResult();
		validationResult.setValid(false);
		validationResult.setStatus(HttpStatus.BAD_REQUEST);
		validationResult.setMessage(errorMessage);

		when(policyValidationStrategy.validateUpdating(any())).thenReturn(validationResult);

		mockMvc.perform(MockMvcRequestBuilders.put("/api/policy").header("Authorization", authorizationHeader)
				.header("X-Policy-Id", policyId).contentType(MediaType.APPLICATION_JSON).content("""
						    {
						      "policyName": ""
						    }
						""")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value(errorMessage));
	}

	@Test
	void testUpdatePolicy_ServiceException() throws Exception {
		String policyId = "abc123";
		String userId = "user001";

		ValidationResult validationResult = new ValidationResult();
		validationResult.setValid(true);
		validationResult.setStatus(HttpStatus.OK);

		when(jwtService.extractSubject("dummy.jwt.token")).thenReturn(userId);
		when(policyValidationStrategy.validateUpdating(any())).thenReturn(validationResult);
		when(policyService.updatePolicy(any(), eq(userId), eq(policyId)))
				.thenThrow(new PolicyServiceException("Unexpected failure"));

		mockMvc.perform(MockMvcRequestBuilders.put("/api/policy").header("Authorization", authorizationHeader)
				.header("X-Policy-Id", policyId).contentType(MediaType.APPLICATION_JSON).content("""
						    {
						      "policyName": "Will Fail"
						    }
						""")).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Unexpected failure"));
	}

	@Test
	void testUpdatePolicy_MissingPolicyIdHeader() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.put("/api/policy").header("Authorization", authorizationHeader)
				.contentType(MediaType.APPLICATION_JSON).content("""
						    {
						      "policyName": "Some Policy"
						    }
						""")).andExpect(status().isUnauthorized());
	}

	@Test
	void testGetPolicyByPolicyId_Success() throws Exception {
		PolicyDTO mockPolicy = new PolicyDTO();
		mockPolicy.setPolicyId(policyId);
		mockPolicy.setPolicyName("Health Policy");
		mockPolicy.setOrganizationId("ORG001");
		ValidationResult validationResult = new ValidationResult();
		validationResult.setValid(true);

		when(policyService.findByPolicyId(policyId)).thenReturn(mockPolicy);
		when(jwtService.extractOrgIdFromToken(token)).thenReturn("ORG001");
		when(policyValidationStrategy.isUserOrganizationValidAndActive("ORG001", "ORG001", authorizationHeader))
		.thenReturn(validationResult);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/policy/my-policy").header("Authorization", authorizationHeader)
				.header("X-Policy-Id", policyId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.policyName").value("Health Policy"));
	}

	@Test
	void testGetPolicyByPolicyId_BlankPolicyId_ReturnsBadRequest() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/policy/my-policy").header("Authorization", authorizationHeader)
				.header("X-Policy-Id", "")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Bad Request: Policy id could not be blank."));
	}

	@Test
	void testGetPolicyByPolicyId_UnauthorizedOrgMismatch() throws Exception {
		PolicyDTO mockPolicy = new PolicyDTO();
		mockPolicy.setPolicyId(policyId);
		mockPolicy.setPolicyName("Confidential Policy");
		mockPolicy.setOrganizationId("ORG999");
		ValidationResult validationResult = new ValidationResult();
		validationResult.setValid(false);


		when(policyService.findByPolicyId(policyId)).thenReturn(mockPolicy);
		when(jwtService.extractOrgIdFromToken(token)).thenReturn("ORG001");
		when(policyValidationStrategy.isUserOrganizationValidAndActive("ORG999", "ORG001", authorizationHeader))
		.thenReturn(validationResult);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/policy/my-policy").header("Authorization", authorizationHeader)
				.header("X-Policy-Id", policyId)).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Unauthorized to view this policy."));
	}

	@Test
	void testGetPolicyByPolicyId_InternalServerError() throws Exception {
		when(policyService.findByPolicyId(policyId)).thenThrow(new PolicyServiceException("Unexpected error"));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/policy/my-policy").header("Authorization", authorizationHeader)
				.header("X-Policy-Id", policyId)).andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Unexpected error"));
	}
}
