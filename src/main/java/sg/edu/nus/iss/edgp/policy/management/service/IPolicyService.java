package sg.edu.nus.iss.edgp.policy.management.service;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

import sg.edu.nus.iss.edgp.policy.management.dto.PolicyDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyRequest;
import sg.edu.nus.iss.edgp.policy.management.entity.Policy;

public interface IPolicyService {

	PolicyDTO createPolicy(PolicyRequest policyReq, String userId);
	
	Policy findByPolicyName(String policyName); 
	
	Map<Long, List<PolicyDTO>> retrievePaginatedPolicyList(Pageable pageable, Boolean isPublished, String orgId);
	
	Map<Long, List<PolicyDTO>> retrieveAllPolicyList(Boolean isPublished, String orgId);
}
