package sg.edu.nus.iss.edgp.policy.management.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.nus.iss.edgp.policy.management.dto.PolicyDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyRequest;
import sg.edu.nus.iss.edgp.policy.management.dto.ValidationResult;
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
        when(policyValidationStrategy.validateCreation(any(), eq(authHeader))).thenReturn(validationResult);
        when(policyService.createPolicy(any(), eq(userId))).thenReturn(policyDTO);

        // Perform the request
        mockMvc.perform(post("/api/policy")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(policyRequest)))
                .andExpect(status().isOk())
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
        when(policyValidationStrategy.validateCreation(any(), eq(authHeader))).thenReturn(validationResult);

        mockMvc.perform(post("/api/policy")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(policyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid input"));
    }

    @Test
    void testCreatePolicy_InternalServerError() throws Exception {
        PolicyRequest policyRequest = new PolicyRequest();
        String jwtToken = "mock-jwt-token";
        String authHeader = "Bearer " + jwtToken;

        when(jwtService.extractSubject(jwtToken)).thenReturn("user-123");
        when(policyValidationStrategy.validateCreation(any(), eq(authHeader)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/policy")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(policyRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists());

    }
}

