package sg.edu.nus.iss.edgp.policy.management.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import sg.edu.nus.iss.edgp.policy.management.entity.Policy;

public interface PolicyRepository extends JpaRepository<Policy, String> {

	Policy findByPolicyName(String policyName);
	
	Page<Policy> findPaginatedByIsPublishedAndOrganizationId(boolean isPublished, String orgId, Pageable pageable);
	
	List<Policy> findAllByOrganizationId(String organizationId);
	
	Page<Policy> findPaginatedByOrganizationId(String orgId, Pageable pageable);
	
	List<Policy> findAllByIsPublishedAndOrganizationId(boolean isPublished, String organizationId);
}
