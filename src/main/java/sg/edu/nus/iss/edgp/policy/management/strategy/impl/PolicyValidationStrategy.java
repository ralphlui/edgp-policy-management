package sg.edu.nus.iss.edgp.policy.management.strategy.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.policy.management.connector.OrganizationAPICall;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyRequest;
import sg.edu.nus.iss.edgp.policy.management.dto.ValidationResult;
import sg.edu.nus.iss.edgp.policy.management.entity.Policy;
import sg.edu.nus.iss.edgp.policy.management.service.impl.JwtService;
import sg.edu.nus.iss.edgp.policy.management.service.impl.PolicyService;
import sg.edu.nus.iss.edgp.policy.management.strategy.IAPIHelperValidationStrategy;
import sg.edu.nus.iss.edgp.policy.management.utility.JSONReader;

@Service
@RequiredArgsConstructor
public class PolicyValidationStrategy implements IAPIHelperValidationStrategy<PolicyRequest> {

	private final PolicyService policyService;
	private final OrganizationAPICall orgAPICall;
	private static final Logger logger = LoggerFactory.getLogger(PolicyValidationStrategy.class);
	private final JSONReader jsonReader;
	private final JwtService jwtService;

	@Override
	public ValidationResult validateCreation(PolicyRequest policyReq, String authorizationHeader) {
		ValidationResult validationResult = new ValidationResult();
		String policyName = policyReq.getPolicyName();
		String domainName = policyReq.getDomainName();
		String organizationId = policyReq.getOrganizationId();

		List<String> missingFields = new ArrayList<>();
		if (policyName == null || policyName.isEmpty())
			missingFields.add("Policy name");
		if (domainName == null || domainName.isEmpty())
			missingFields.add("Domain name");
		if (organizationId == null || organizationId.isEmpty())
			missingFields.add("Organization ID");

		if (!missingFields.isEmpty()) {
			return buildInvalidResult(String.join(" and ", missingFields) + " is required");
		}

		String jwtToken = authorizationHeader.substring(7);
		String userOrgId = jwtService.extractOrgIdFromToken(jwtToken);
		if (!organizationId.equals(userOrgId)) {
			return buildInvalidResult("Access Denied. Not authorized to create policy for this organization.");
		}

		Policy dbPolicy = policyService.findByPolicyName(policyReq.getPolicyName().trim());
		if (dbPolicy != null) {
			validationResult.setMessage("Duplicate policy detected. Please enter a unique name.");
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		Boolean isActive = validateActiveOrganization(organizationId, authorizationHeader);
		if (!isActive) {
			return buildInvalidResult("Invalid organization. Unable to create policy.");
		}

		validationResult.setValid(true);

		return validationResult;
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
