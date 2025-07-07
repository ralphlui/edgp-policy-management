package sg.edu.nus.iss.edgp.policy.management.utility;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import sg.edu.nus.iss.edgp.policy.management.dto.PolicyDTO;
import sg.edu.nus.iss.edgp.policy.management.entity.Policy;
import sg.edu.nus.iss.edgp.policy.management.entity.Rule;

public class DTOMapperTest {

	@Test
    public void testToPolicyDTO_AllFieldsMappedCorrectly() {
		
		Rule rule1 = new Rule();
		rule1.setRuleName("Rule1");
		rule1.setAppliesToField("field1");
		rule1.setDescription("Description 1");
		rule1.setParameters(Map.of("min", 1, "max", 10));
		
        // Arrange
        Policy policy = new Policy();
        policy.setPolicyId("P123");
        policy.setPolicyName("Test Policy");
        policy.setDomainName("Test Domain");
        policy.setDescription("Policy Description");
        policy.setPublished(true);
        policy.setCreatedBy("creatorUser");
        policy.setLastUpdatedBy("updaterUser");
        policy.setRules(List.of(rule1));
        policy.setOrganizationId("ORG456");

        // Act
        PolicyDTO policyDTO = DTOMapper.toPolicyDTO(policy); // assuming the method is in a class called PolicyMapper

        // Assert
        assertNotNull(policyDTO);
        assertEquals(policy.getPolicyId(), policyDTO.getPolicyId());
        assertEquals(policy.getPolicyName(), policyDTO.getPolicyName());
        assertEquals(policy.getDomainName(), policyDTO.getDomainName());
        assertEquals(policy.getDescription(), policyDTO.getDescription());
        assertEquals(policy.isPublished(), policyDTO.isPublished());
        assertEquals(policy.getCreatedBy(), policyDTO.getCreatedBy());
        assertEquals(policy.getLastUpdatedBy(), policyDTO.getLastUpdatedBy());
        assertEquals(policy.getRules(), policyDTO.getRules());
        assertEquals(policy.getOrganizationId(), policyDTO.getOrganizationId());
    }
}
