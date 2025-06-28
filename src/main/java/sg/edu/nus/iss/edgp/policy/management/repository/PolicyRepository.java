package sg.edu.nus.iss.edgp.policy.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import sg.edu.nus.iss.edgp.policy.management.entity.Policy;

public interface PolicyRepository extends JpaRepository<Policy, String> {

	@Query("SELECT p FROM Policy p WHERE p.policyName = ?1")
	Policy findByPolicyName(String policyName);
}
