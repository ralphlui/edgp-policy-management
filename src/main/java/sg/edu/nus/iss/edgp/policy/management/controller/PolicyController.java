package sg.edu.nus.iss.edgp.policy.management.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.policy.management.dto.APIResponse;
import sg.edu.nus.iss.edgp.policy.management.dto.AuditDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyRequest;
import sg.edu.nus.iss.edgp.policy.management.dto.ValidationResult;
import sg.edu.nus.iss.edgp.policy.management.exception.PolicyServiceException;
import sg.edu.nus.iss.edgp.policy.management.service.impl.AuditService;
import sg.edu.nus.iss.edgp.policy.management.service.impl.JwtService;
import sg.edu.nus.iss.edgp.policy.management.service.impl.PolicyService;
import sg.edu.nus.iss.edgp.policy.management.strategy.impl.PolicyValidationStrategy;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/policy")
public class PolicyController {

	private static final Logger logger = LoggerFactory.getLogger(PolicyController.class);
	private final JwtService jwtService;
	private final PolicyValidationStrategy policyValidationStrategy;
	private final PolicyService policyService;
	private final AuditService auditService;
	private String genericErrorMessage = "An error occurred while processing your request. Please try again later.";

	@PostMapping(value = "", produces = "application/json")
	@PreAuthorize("hasAuthority('SCOPE_manage:policy')")
	public ResponseEntity<APIResponse<PolicyDTO>> createPolicy(
			@RequestHeader("Authorization") String authorizationHeader, @RequestBody PolicyRequest policyRequest) {
		logger.info("Calling create policy API ...");
		String message = "";
		String activityType = "Create Policy";
		String endpoint = "/api/policy";
		String httpMethod = HttpMethod.POST.name();

		AuditDTO auditDTO = auditService.createAuditDTO(activityType, endpoint, httpMethod);

		try {
			String jwtToken = authorizationHeader.substring(7);
			String userId = jwtService.extractSubject(jwtToken);
			ValidationResult validationResult = policyValidationStrategy.validateCreation(policyRequest, authorizationHeader);

			if (validationResult.isValid()) {
				PolicyDTO policyDTO = policyService.createPolicy(policyRequest, userId);
				message = "Success! The new policy has been added.";
				logger.info(message);
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(policyDTO, message));
			} else {
				message = validationResult.getMessage();
				logger.error(message);
				auditService.logAudit(auditDTO, validationResult.getStatus().value(), message, authorizationHeader);
				return ResponseEntity.status(validationResult.getStatus()).body(APIResponse.error(message));

			}

		} catch (Exception e) {
			message = e instanceof PolicyServiceException ? e.getMessage() : genericErrorMessage;
			logger.error(message);
			auditService.logAudit(auditDTO, 500, message, authorizationHeader);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}

	}

}
