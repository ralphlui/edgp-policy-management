package sg.edu.nus.iss.edgp.policy.management.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyRequest;
import sg.edu.nus.iss.edgp.policy.management.entity.Policy;
import sg.edu.nus.iss.edgp.policy.management.exception.PolicyServiceException;
import sg.edu.nus.iss.edgp.policy.management.repository.PolicyRepository;
import sg.edu.nus.iss.edgp.policy.management.service.IPolicyService;
import sg.edu.nus.iss.edgp.policy.management.utility.DTOMapper;

@Service
@RequiredArgsConstructor
public class PolicyService implements IPolicyService {
	
	private static final Logger logger = LoggerFactory.getLogger(PolicyService.class);
	private final PolicyRepository policyRepository;
	
	@Override
	public PolicyDTO createPolicy(PolicyRequest policyReq, String userId) {
		try {

			Policy policy = new Policy();
			policy.setPolicyName(policyReq.getPolicyName());
			policy.setDescription(policyReq.getDescription());
			policy.setDomainName(policyReq.getDomainName());
			policy.setPublished(policyReq.isPublished());
			policy.setRules(policyReq.getRules());
			policy.setCreatedBy(userId);
			policy.setLastUpdatedBy(userId);
			policy.setOrganizationId(policyReq.getOrganizationId());
			logger.info("Creating policy ....");
			Policy createdPolicy = policyRepository.save(policy);
			return DTOMapper.toPolicyDTO(createdPolicy);
		} catch (Exception ex) {
			logger.error("Exception occurred while creating policy", ex);
			throw new PolicyServiceException("An error occured while creating policy", ex);
		}
	}

	@Override
	public Policy findByPolicyName(String policyName) {
		try {
			return policyRepository.findByPolicyName(policyName);
		} catch (Exception ex) {
			logger.error("Exception occurred while searching for the policy by name", ex);
			throw new PolicyServiceException("An error occurred while searching for the policy by name", ex);
		}

	}

}
