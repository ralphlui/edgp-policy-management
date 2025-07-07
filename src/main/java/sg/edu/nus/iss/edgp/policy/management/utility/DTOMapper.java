package sg.edu.nus.iss.edgp.policy.management.utility;

import org.springframework.stereotype.Component;

import sg.edu.nus.iss.edgp.policy.management.dto.PolicyDTO;
import sg.edu.nus.iss.edgp.policy.management.entity.Policy;

@Component
public class DTOMapper {

	
	public static PolicyDTO toPolicyDTO(Policy policy) {
		PolicyDTO policyDTO = new PolicyDTO();
		policyDTO.setPolicyId(policy.getPolicyId());
		policyDTO.setPolicyName(policy.getPolicyName());
		policyDTO.setDomainName(policy.getDomainName());
		policyDTO.setDescription(policy.getDescription());
		policyDTO.setPublished(policy.isPublished());
		policyDTO.setCreatedBy(policy.getCreatedBy());
		policyDTO.setLastUpdatedBy(policy.getLastUpdatedBy());
		policyDTO.setRules(policy.getRules());
		policyDTO.setOrganizationId(policy.getOrganizationId());
		return policyDTO;
	}
}
