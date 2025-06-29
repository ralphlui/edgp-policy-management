package sg.edu.nus.iss.edgp.policy.management.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.policy.management.dto.APIResponse;
import sg.edu.nus.iss.edgp.policy.management.dto.AuditDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyRequest;
import sg.edu.nus.iss.edgp.policy.management.dto.SearchRequest;
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
	
	
	@GetMapping(value = "", produces = "application/json")
	@PreAuthorize("hasAuthority('SCOPE_manage:policy') or hasAuthority('SCOPE_view:policy')")
	public ResponseEntity<APIResponse<List<PolicyDTO>>> retrievePolicyList(
			@RequestHeader("Authorization") String authorizationHeader,
			@Valid @ModelAttribute SearchRequest searchRequest) {

		logger.info("Call policy getAll API with page={}, size={}", searchRequest.getPage(), searchRequest.getSize());
		String message = "";
		String activityType = "Retrieve Policy List";
		String endpoint = "/api/policy";
		String httpMethod = HttpMethod.GET.name();
		AuditDTO auditDTO = auditService.createAuditDTO(activityType, endpoint, httpMethod);


		try {
			
			Map<Long, List<PolicyDTO>> resultMap;
			String jwtToken = authorizationHeader.substring(7);
			String userOrgId = jwtService.extractOrgIdFromToken(jwtToken);
			
			if (userOrgId == null || userOrgId.isEmpty()) {
				message = "Organization ID missing or invalid in token";
				auditService.logAudit(auditDTO, 400, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}
			
			
			 if (searchRequest.getPage() == null) {
				 resultMap = policyService.retrieveAllPolicyList(searchRequest.getIsPublished(), userOrgId);
					logger.info("all policy list size {}", resultMap.size());
			 } else {
				 Pageable pageable = PageRequest.of(searchRequest.getPage() - 1, searchRequest.getSize(),
							Sort.by("policyName").ascending());
				 resultMap = policyService.retrievePaginatedPolicyList(pageable, searchRequest.getIsPublished(), userOrgId);
				 logger.info("all paginated policy list size {}", resultMap.size());
			 }
			

			Map.Entry<Long, List<PolicyDTO>> firstEntry = resultMap.entrySet().iterator().next();
			long totalRecord = firstEntry.getKey();
			List<PolicyDTO> policyDTOList = firstEntry.getValue();

			logger.info("totalRecord: {}", totalRecord);

			if (!policyDTOList.isEmpty()) {
				message = "Successfully retrieved all policy list.";
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.success(policyDTOList, message, totalRecord));

			} else {
				message = "No policy List.";
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.successWithEmptyData(policyDTOList, message));
			}

		} catch (Exception ex) {
			message = ex instanceof PolicyServiceException ? ex.getMessage() : genericErrorMessage;
			auditService.logAudit(auditDTO, 500, message, authorizationHeader);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}
	}
	

}
