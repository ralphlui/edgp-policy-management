package sg.edu.nus.iss.edgp.policy.management.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	@Override
	public Map<Long, List<PolicyDTO>> retrievePaginatedPolicyList(Pageable pageable, Boolean isPublished,
			String orgId) {
		try {
			List<PolicyDTO> policyDTOList = new ArrayList<>();
			Page<Policy> policyPages;
			if (isPublished == null) {
				policyPages = policyRepository.findPaginatedByOrganizationId(orgId, pageable);
			} else {
				policyPages = policyRepository.findPaginatedByIsPublishedAndOrganizationId(isPublished, orgId,
						pageable);
			}

			long totalRecord = policyPages.getTotalElements();
			if (totalRecord > 0) {
				logger.info("Paginated policy list is found.");
				for (Policy policy : policyPages.getContent()) {
					PolicyDTO policyDTO = DTOMapper.toPolicyDTO(policy);
					policyDTOList.add(policyDTO);
				}
			}
			Map<Long, List<PolicyDTO>> result = new HashMap<>();
			result.put(totalRecord, policyDTOList);
			return result;

		} catch (Exception ex) {
			logger.error("Exception occurred while retrieving paginated policy list", ex);
			throw new PolicyServiceException("An error occurred while retrieving paginated policy list", ex);

		}
	}

	@Override
	public Map<Long, List<PolicyDTO>> retrieveAllPolicyList(Boolean isPublished, String orgId) {
		try {
			 List<Policy> dbPolicyList;
			if (isPublished == null) {
	            dbPolicyList = policyRepository.findAllByOrganizationId(orgId);
	        } else {
	            dbPolicyList = policyRepository.findAllByIsPublishedAndOrganizationId(isPublished, orgId);
	        }
			long totalRecord = dbPolicyList.size();
			List<PolicyDTO> policyDTOList = new ArrayList<>();
			if (totalRecord > 0) {
				logger.info("All policy list is found.");
				for (Policy policy : dbPolicyList) {
					PolicyDTO policyDTO = DTOMapper.toPolicyDTO(policy);
					policyDTOList.add(policyDTO);
				}
			}
			Map<Long, List<PolicyDTO>> result = new HashMap<>();
			result.put(totalRecord, policyDTOList);
			return result;

		} catch (Exception ex) {
			logger.error("Exception occurred while retrieving all policy list", ex);
			throw new PolicyServiceException("An error occurred while retrieving all policy list", ex);

		}
	}

}
