package sg.edu.nus.iss.edgp.policy.management.utility;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import sg.edu.nus.iss.edgp.policy.management.dto.PolicyDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.Rule;
import sg.edu.nus.iss.edgp.policy.management.entity.AppliedRule;
import sg.edu.nus.iss.edgp.policy.management.entity.Policy;

import java.util.*;

public class DTOMapperTest {

	@Test
	void testToPolicyDTO_basicFields() {
		Policy policy = new Policy();
		policy.setPolicyId("P1");
		policy.setPolicyName("PolicyName");
		policy.setDomainName("Domain");
		policy.setDescription("Test policy");
		policy.setPublished(true);
		policy.setCreatedBy("creator");
		policy.setLastUpdatedBy("updater");
		policy.setOrganizationId("ORG123");

		PolicyDTO dto = DTOMapper.toPolicyDTO(policy);

		assertEquals("P1", dto.getPolicyId());
		assertEquals("PolicyName", dto.getPolicyName());
		assertEquals("Domain", dto.getDomainName());
		assertEquals("Test policy", dto.getDescription());
		assertTrue(dto.isPublished());
		assertEquals("creator", dto.getCreatedBy());
		assertEquals("updater", dto.getLastUpdatedBy());
		assertEquals("ORG123", dto.getOrganizationId());
		assertTrue(dto.getRules() == null || dto.getRules().isEmpty(),
				"rules should be null or empty when no applied rules");
	}

	@Test
	void testToPolicyDTO_withNullAppliedRules() {
		Policy policy = new Policy();
		policy.setPolicyId("P2");
		policy.setAppliedRules(null); // explicitly null

		PolicyDTO dto = DTOMapper.toPolicyDTO(policy);

		assertEquals("P2", dto.getPolicyId());
		assertTrue(dto.getRules() == null || dto.getRules().isEmpty(),
				"rules should be null or empty when appliedRules is null");
	}

	@Test
	void testToPolicyDTO_withAppliedRules() {
		AppliedRule ar = new AppliedRule();
		ar.setAppliesToField("field1, field2");
		ar.setDescription(" A rule description ");
		ar.setRuleName(" RuleOne ");
		Map<String, Object> params = new HashMap<>();
		params.put("key1", "val1");
		ar.setParameters(params);

		Policy policy = new Policy();
		policy.setPolicyId("P3");
		policy.setAppliedRules(Collections.singletonList(ar));

		PolicyDTO dto = DTOMapper.toPolicyDTO(policy);

		assertNotNull(dto.getRules());
		assertEquals(1, dto.getRules().size());

		Rule rule = dto.getRules().get(0);
		assertEquals(Arrays.asList("field1", "field2"), rule.getAppliesToField());
		assertEquals("A rule description", rule.getDescription());
		assertEquals("RuleOne", rule.getRuleName());
		assertEquals("val1", rule.getParameters().get("key1"));
	}

	@Test
	void testToPolicyDTO_withEmptyAppliedRule() {
		AppliedRule emptyRule = new AppliedRule(); // all fields null/empty
		Policy policy = new Policy();
		policy.setAppliedRules(Collections.singletonList(emptyRule));

		PolicyDTO dto = DTOMapper.toPolicyDTO(policy);

		// Rule should not be added because nothing was populated
		assertTrue(dto.getRules() == null || dto.getRules().isEmpty(),
				"rules should be null or empty when appliedRule is empty");
	}
}