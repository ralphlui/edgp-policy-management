package sg.edu.nus.iss.edgp.policy.management.strategy.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyRequest;
import sg.edu.nus.iss.edgp.policy.management.dto.ValidationResult;
import sg.edu.nus.iss.edgp.policy.management.entity.Policy;
import sg.edu.nus.iss.edgp.policy.management.service.impl.PolicyService;
import sg.edu.nus.iss.edgp.policy.management.strategy.IAPIHelperValidationStrategy;


@Service
@RequiredArgsConstructor
public class PolicyValidationStrategy implements IAPIHelperValidationStrategy<PolicyRequest> {
	
	private final PolicyService policyService;
	
	@Override
	public ValidationResult validateCreation(PolicyRequest policyReq) {
	ValidationResult validationResult = new ValidationResult();
	String policyName = policyReq.getPolicyName();
	String domainName = policyReq.getDomainName();

	List<String> missingFields = new ArrayList<>();
	if (policyName == null || policyName.isEmpty())
		missingFields.add("Policy name");
	if (domainName == null || domainName.isEmpty())
		missingFields.add("Domain name");

	if (!missingFields.isEmpty()) {
		validationResult.setMessage(String.join(" and ", missingFields) + " is required");
		validationResult.setStatus(HttpStatus.BAD_REQUEST);
		validationResult.setValid(false);
		return validationResult;
	}

	Policy dbPolicy = policyService.findByPolicyName(policyReq.getPolicyName().trim());
	if (dbPolicy != null) {
		validationResult.setMessage("Duplicate policy detected. Please enter a unique name.");
		validationResult.setStatus(HttpStatus.BAD_REQUEST);
		validationResult.setValid(false);
		return validationResult;
	}

	validationResult.setValid(true);

	return validationResult;
	}

	
}
