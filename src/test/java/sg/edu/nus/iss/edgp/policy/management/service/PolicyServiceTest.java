package sg.edu.nus.iss.edgp.policy.management.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import sg.edu.nus.iss.edgp.policy.management.dto.PolicyDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyRequest;
import sg.edu.nus.iss.edgp.policy.management.entity.Policy;
import sg.edu.nus.iss.edgp.policy.management.entity.Rule;
import sg.edu.nus.iss.edgp.policy.management.exception.PolicyServiceException;
import sg.edu.nus.iss.edgp.policy.management.repository.PolicyRepository;
import sg.edu.nus.iss.edgp.policy.management.service.impl.PolicyService;
import sg.edu.nus.iss.edgp.policy.management.utility.DTOMapper;

@ExtendWith(MockitoExtension.class)
public class PolicyServiceTest {

	@Mock
	private PolicyRepository policyRepository;

	@InjectMocks
	private PolicyService policyService;

	private PolicyRequest policyRequest;
	private Policy savedPolicy;
	private PolicyDTO expectedDto;
	private final String userId = "test-user";

	@BeforeEach
	void setup() {

		Rule rule1 = new Rule();
		rule1.setRuleName("Rule1");
		rule1.setAppliesToField("field1");
		rule1.setDescription("Description 1");
		rule1.setParameters(Map.of("min", 1, "max", 10));

		Rule rule2 = new Rule();
		rule2.setRuleName("Rule2");
		rule2.setAppliesToField("field2");
		rule2.setDescription("Description 2");
		rule2.setParameters(Map.of("pattern", "[a-z]+"));

		policyRequest = new PolicyRequest();
		policyRequest.setPolicyName("Test Policy");
		policyRequest.setDescription("Test Description");
		policyRequest.setDomainName("Security");
		policyRequest.setPublished(true);
		policyRequest.setRules(List.of(rule1, rule2));
		policyRequest.setOrganizationId("org-123");

		savedPolicy = new Policy();
		savedPolicy.setPolicyId("policy-1");
		savedPolicy.setPolicyName("Test Policy");
		savedPolicy.setDescription("Test Description");
		savedPolicy.setDomainName("Security");
		savedPolicy.setPublished(true);
		savedPolicy.setRules(List.of(rule1, rule2));
		savedPolicy.setCreatedBy(userId);
		savedPolicy.setLastUpdatedBy(userId);
		savedPolicy.setOrganizationId("org-123");

		expectedDto = new PolicyDTO();
		expectedDto.setPolicyId("policy-1");
		expectedDto.setPolicyName("Test Policy");
		expectedDto.setDescription("Test Description");
	}

	@Test
	void testCreatePolicy_success() {
		when(policyRepository.save(any(Policy.class))).thenReturn(savedPolicy);

		try (MockedStatic<DTOMapper> mockedMapper = mockStatic(DTOMapper.class)) {
			mockedMapper.when(() -> DTOMapper.toPolicyDTO(savedPolicy)).thenReturn(expectedDto);

			PolicyDTO result = policyService.createPolicy(policyRequest, userId);

			assertNotNull(result);
			assertEquals(expectedDto.getPolicyId(), result.getPolicyId());
			assertEquals(expectedDto.getPolicyName(), result.getPolicyName());
			verify(policyRepository).save(any(Policy.class));
		}
	}

	@Test
	void testCreatePolicy_exception() {
		when(policyRepository.save(any(Policy.class))).thenThrow(new RuntimeException("DB failure"));

		PolicyServiceException exception = assertThrows(PolicyServiceException.class, () -> {
			policyService.createPolicy(policyRequest, userId);
		});

		assertTrue(exception.getMessage().contains("An error occured while creating policy"));
	}
}
