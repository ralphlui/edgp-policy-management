package sg.edu.nus.iss.edgp.policy.management.strategy.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.policy.management.connector.OrganizationAPICall;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyRequest;
import sg.edu.nus.iss.edgp.policy.management.dto.ValidationResult;
import sg.edu.nus.iss.edgp.policy.management.entity.Policy;
import sg.edu.nus.iss.edgp.policy.management.service.impl.PolicyService;
import sg.edu.nus.iss.edgp.policy.management.strategy.IAPIHelperValidationStrategy;
import sg.edu.nus.iss.edgp.policy.management.utility.GeneralUtility;
import sg.edu.nus.iss.edgp.policy.management.utility.JSONReader;

@Service
@RequiredArgsConstructor
public class PolicyValidationStrategy implements IAPIHelperValidationStrategy<PolicyRequest> {

	private final PolicyService policyService;
	private final OrganizationAPICall orgAPICall;
	private static final Logger logger = LoggerFactory.getLogger(PolicyValidationStrategy.class);
	private final JSONReader jsonReader;


	@Override
	public ValidationResult validateCreation(PolicyRequest policyReq, String authorizationHeader, String userOrgId) {
		ValidationResult validationResult = new ValidationResult();
		String policyName = policyReq.getPolicyName();

		List<String> missingFields = new ArrayList<>();
		if (policyName == null || policyName.isEmpty())
			missingFields.add("Policy name");

		if (!missingFields.isEmpty()) {
			return buildInvalidResult(String.join(" and ", missingFields) + " is required");
		}


		Policy dbPolicy = policyService.findByPolicyName(policyReq.getPolicyName().trim());
		if (dbPolicy != null) {
			validationResult.setMessage("Duplicate policy detected. Please enter a unique name.");
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		Boolean isActive = validateActiveOrganization(userOrgId, authorizationHeader);
		if (!isActive) {
			return buildInvalidResult("Invalid organization. Unable to create policy.");
		}

		validationResult.setValid(true);

		return validationResult;
	}
	
	@Override
	public ValidationResult validateUpdating(PolicyRequest policyReq) {
		ValidationResult validationResult = new ValidationResult();

		String policyId = GeneralUtility.makeNotNull(policyReq.getPolicyId());

		if (policyId.isEmpty()) {
			return buildInvalidResult("Bad Request: Policy ID could not be blank.");
		}

		PolicyDTO policyDTO = policyService.findByPolicyId(policyId);
		if (policyDTO == null || policyDTO.getPolicyId().isEmpty()) {
			return buildInvalidResult("Invalid policy ID.");
		}

		validationResult.setValid(true);
		return validationResult;
	}
	
	public ValidationResult isUserOrganizationActive(String userOrgId, String authHeader) {
	    return validateActive(userOrgId, authHeader);
	}

	public ValidationResult isUserOrganizationValidAndActive(String orgId, String userOrgId, String authHeader) {
	   
	    ValidationResult activeCheck = validateActive(userOrgId, authHeader);
	    if (!activeCheck.isValid()) return activeCheck;

	    if (!Objects.equals(userOrgId, orgId)) {
	        return buildInvalidResult("Unauthorized to view this policy.");
	    }

	    return activeCheck; 
	}

	
	private ValidationResult validateActive(String userOrgId, String authHeader) {
	    if (isBlank(userOrgId)) {
	        return buildInvalidResult("Organization ID missing or invalid in token");
	    }

	    boolean isActive = Boolean.TRUE.equals(validateActiveOrganization(userOrgId, authHeader));
	    if (!isActive) {
	        return buildInvalidResult("Invalid organization. Unable to view policy.");
	    }

	    ValidationResult validationResult = new ValidationResult();
	    validationResult.setValid(true);
	    return validationResult;
	}

	/** Tiny utility (avoids external deps). */
	private boolean isBlank(String s) {
	    return s == null || s.trim().isEmpty();
	}

	private ValidationResult buildInvalidResult(String message) {
		ValidationResult result = new ValidationResult();
		result.setMessage(message);
		result.setValid(false);
		result.setStatus(HttpStatus.BAD_REQUEST);
		return result;
	}

	private Boolean validateActiveOrganization(String orgId, String authHeader) {
		String responseStr = orgAPICall.validateActiveOrganization(orgId, authHeader);
		try {
			JSONParser parser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);
			Boolean success = jsonReader.getSuccessFromResponse(jsonResponse);

			if (success) {
				JSONObject data = jsonReader.getDataFromResponse(jsonResponse);
				if (data != null) {
					Boolean isActive = (Boolean) data.get("active");
					return isActive;
				}
			}
			return false;

		} catch (ParseException e) {
			logger.error("Error parsing JSON response for validating active organization...", e);
			return false;
		}

	}

}
