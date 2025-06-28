package sg.edu.nus.iss.edgp.policy.management.service;

import sg.edu.nus.iss.edgp.policy.management.dto.PolicyDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyRequest;
import sg.edu.nus.iss.edgp.policy.management.entity.Policy;

public interface IPolicyService {

	PolicyDTO createPolicy(PolicyRequest policyReq, String userId);
	
	Policy findByPolicyName(String policyName); 
}
