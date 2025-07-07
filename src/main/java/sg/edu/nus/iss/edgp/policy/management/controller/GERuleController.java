package sg.edu.nus.iss.edgp.policy.management.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.policy.management.dto.APIResponse;
import sg.edu.nus.iss.edgp.policy.management.dto.AuditDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.GERuleDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.SearchRequest;
import sg.edu.nus.iss.edgp.policy.management.exception.GERuleServiceException;
import sg.edu.nus.iss.edgp.policy.management.service.impl.AuditService;
import sg.edu.nus.iss.edgp.policy.management.service.impl.GERuleService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/policy/gerule")
public class GERuleController {
	
	private static final Logger logger = LoggerFactory.getLogger(PolicyController.class);
	private final GERuleService geRuleService;
	private final AuditService auditService;
	private String genericErrorMessage = "An error occurred while processing your request. Please try again later.";


	@GetMapping(value = "", produces = "application/json")
	@PreAuthorize("hasAuthority('SCOPE_manage:policy') or hasAuthority('SCOPE_view:policy')")
	public ResponseEntity<APIResponse<List<GERuleDTO>>> retrieveGERuleList(
			@RequestHeader("Authorization") String authorizationHeader,
			@Valid @ModelAttribute SearchRequest searchRequest) {

		logger.info("Call retrieving GE rule list API with page={}, size={}", searchRequest.getPage(), searchRequest.getSize());
		String message = "";
		String activityType = "Retrieve GE Rule List";
		String endpoint = "/api/policy/gerule";
		String httpMethod = HttpMethod.GET.name();
		AuditDTO auditDTO = auditService.createAuditDTO(activityType, endpoint, httpMethod);


		try {
			
			Map<Long, List<GERuleDTO>> resultMap;
			
			 resultMap = geRuleService.retrieveGERuleList(authorizationHeader, searchRequest);
				logger.info("all GE rules list size {}", resultMap.size());
			

			Map.Entry<Long, List<GERuleDTO>> firstEntry = resultMap.entrySet().iterator().next();
			long totalRecord = firstEntry.getKey();
			List<GERuleDTO> geRuleDTOList = firstEntry.getValue();

			logger.info("totalRecord: {}", totalRecord);

			if (!geRuleDTOList.isEmpty()) {
				message = "Successfully retrieved all GE rule list.";
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.success(geRuleDTOList, message, totalRecord));

			} else {
				message = "No GE rule List.";
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.successWithEmptyData(geRuleDTOList, message));
			}

		} catch (Exception ex) {
			message = ex instanceof GERuleServiceException ? ex.getMessage() : genericErrorMessage;
			auditService.logAudit(auditDTO, 500, message, authorizationHeader);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}
	}
	
	
}
