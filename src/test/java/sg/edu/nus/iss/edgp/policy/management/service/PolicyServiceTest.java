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
import java.util.Optional;

import org.springframework.data.domain.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import sg.edu.nus.iss.edgp.policy.management.dto.PolicyDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.PolicyRequest;
import sg.edu.nus.iss.edgp.policy.management.dto.SearchRequest;
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
	private Policy policy;
	private Pageable pageable;
	private String orgId = "org-123";
	private PolicyDTO policyDTO;
	private Policy updatedPolicy;
	private final String policyId = "policy-123";
	private final SearchRequest searchRequest = new SearchRequest();

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
		savedPolicy.setPolicyId(policyId);
		savedPolicy.setPolicyName("Test Policy");
		savedPolicy.setDescription("Test Description");
		savedPolicy.setDomainName("Security");
		savedPolicy.setPublished(true);
		savedPolicy.setRules(List.of(rule1, rule2));
		savedPolicy.setCreatedBy(userId);
		savedPolicy.setLastUpdatedBy(userId);
		savedPolicy.setOrganizationId("org-123");

		expectedDto = new PolicyDTO();
		expectedDto.setPolicyId(policyId);
		expectedDto.setPolicyName("Test Policy");
		expectedDto.setDescription("Test Description");

		policy = new Policy();
		policy.setPolicyId("policy-1");
		policy.setPolicyName("DataRetentionPolicy");
		policy.setDescription("Test policy description");

		pageable = PageRequest.of(0, 10);

		policyDTO = new PolicyDTO();
		policyDTO.setPolicyId(policyId);
		policyDTO.setPolicyName("RetentionPolicy");
		policyDTO.setDescription("Latest Descritpion");

		updatedPolicy = new Policy();
		updatedPolicy.setPolicyId(policyId);
		updatedPolicy.setDescription("Latest Description");
		updatedPolicy.setPublished(true);
		updatedPolicy.setLastUpdatedBy(userId);

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

	@Test
	void testFindByPolicyName_success() {
		when(policyRepository.findByPolicyName("DataRetentionPolicy")).thenReturn(policy);

		Policy result = policyService.findByPolicyName("DataRetentionPolicy");

		assertNotNull(result);
		assertEquals("policy-1", result.getPolicyId());
		assertEquals("DataRetentionPolicy", result.getPolicyName());
		verify(policyRepository).findByPolicyName("DataRetentionPolicy");
	}

	@Test
	void testFindByPolicyName_throwsException() {
		when(policyRepository.findByPolicyName("DataRetentionPolicy")).thenThrow(new RuntimeException("DB error"));

		Exception exception = assertThrows(PolicyServiceException.class, () -> {
			policyService.findByPolicyName("DataRetentionPolicy");
		});

		assertTrue(exception.getMessage().contains("An error occurred while searching for the policy by name"));
	}

	@Test
	void testRetrievePaginatedPolicyList_WithPublishedFilter() {
		List<Policy> policyList = List.of(policy);
		Page<Policy> page = new PageImpl<>(policyList, pageable, 1);
		searchRequest.setIsPublished(true);

		when(policyRepository.findPaginatedByIsPublishedAndOrganizationId(true, orgId, pageable)).thenReturn(page);

		try (MockedStatic<DTOMapper> mockedMapper = mockStatic(DTOMapper.class)) {
			mockedMapper.when(() -> DTOMapper.toPolicyDTO(policy)).thenReturn(policyDTO);

			Map<Long, List<PolicyDTO>> result = policyService.retrievePaginatedPolicyList(pageable, searchRequest, orgId);

			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(1, result.entrySet().iterator().next().getValue().size());
			assertEquals(policyId, result.entrySet().iterator().next().getValue().get(0).getPolicyId());
		}
	}

	@Test
	void testRetrievePaginatedPolicyList_WithoutPublishedFilter() {
		List<Policy> policyList = List.of(policy);
		Page<Policy> page = new PageImpl<>(policyList, pageable, 1);

		when(policyRepository.findPaginatedByOrganizationId(orgId, pageable)).thenReturn(page);

		try (MockedStatic<DTOMapper> mockedMapper = mockStatic(DTOMapper.class)) {
			mockedMapper.when(() -> DTOMapper.toPolicyDTO(policy)).thenReturn(policyDTO);
			
			SearchRequest searchRequest = new SearchRequest();
			Map<Long, List<PolicyDTO>> result = policyService.retrievePaginatedPolicyList(pageable, searchRequest, orgId);

			assertNotNull(result);
			assertEquals(1, result.size());
		}
	}

	@Test
	void testRetrievePaginatedPolicyList_ThrowsException() {
		when(policyRepository.findPaginatedByOrganizationId(orgId, pageable))
				.thenThrow(new RuntimeException("DB error"));

		Exception exception = assertThrows(PolicyServiceException.class, () -> {
			policyService.retrievePaginatedPolicyList(pageable, searchRequest, orgId);
		});

		assertTrue(exception.getMessage().contains("An error occurred while retrieving paginated policy list"));
	}

	@Test
	void testRetrieveAllPolicyList_WithPublishedFilter() {
		List<Policy> policies = List.of(policy);
		when(policyRepository.findAllByIsPublishedAndOrganizationId(true, orgId)).thenReturn(policies);

		try (MockedStatic<DTOMapper> mapper = mockStatic(DTOMapper.class)) {
			mapper.when(() -> DTOMapper.toPolicyDTO(policy)).thenReturn(policyDTO);

			searchRequest.setIsPublished(true);
			Map<Long, List<PolicyDTO>> result = policyService.retrieveAllPolicyList(searchRequest, orgId);

			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(policyId, result.values().iterator().next().get(0).getPolicyId());
		}
	}

	@Test
	void testRetrieveAllPolicyList_WithoutPublishedFilter() {
		List<Policy> policies = List.of(policy);
		when(policyRepository.findAllByOrganizationId(orgId)).thenReturn(policies);

		try (MockedStatic<DTOMapper> mapper = mockStatic(DTOMapper.class)) {
			mapper.when(() -> DTOMapper.toPolicyDTO(policy)).thenReturn(policyDTO);

			Map<Long, List<PolicyDTO>> result = policyService.retrieveAllPolicyList(searchRequest, orgId);

			assertNotNull(result);
		}
	}

	@Test
	void testRetrieveAllPolicyList_ThrowsException() {
		when(policyRepository.findAllByOrganizationId(orgId)).thenThrow(new RuntimeException("DB issue"));

		PolicyServiceException exception = assertThrows(PolicyServiceException.class,
				() -> policyService.retrieveAllPolicyList(searchRequest, orgId));

		assertTrue(exception.getMessage().contains("An error occurred while retrieving all policy list"));
	}

	@Test
	void testUpdatePolicy_success() {
		when(policyRepository.findByPolicyId(policyId)).thenReturn(Optional.of(policy));
		when(policyRepository.save(any(Policy.class))).thenReturn(updatedPolicy);

		try (MockedStatic<DTOMapper> mapper = mockStatic(DTOMapper.class)) {
			mapper.when(() -> DTOMapper.toPolicyDTO(updatedPolicy)).thenReturn(policyDTO);

			PolicyDTO result = policyService.updatePolicy(policyRequest, userId, policyId);

			assertNotNull(result);
			assertEquals("Latest Descritpion", result.getDescription());
			verify(policyRepository).save(policy);
		}
	}

	@Test
	void testUpdatePolicy_exceptionThrownDuringUpdate() {
		when(policyRepository.findByPolicyId(policyId)).thenReturn(Optional.of(policy));
		when(policyRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

		PolicyServiceException ex = assertThrows(PolicyServiceException.class, () -> {
			policyService.updatePolicy(policyRequest, userId, policyId);
		});

		assertTrue(ex.getMessage().contains("An error occurred while updating policy"));
	}

	@Test
	void testFindByPolicyId_success() {
		when(policyRepository.findByPolicyId(policyId)).thenReturn(Optional.of(policy));

		try (MockedStatic<DTOMapper> mockedMapper = mockStatic(DTOMapper.class)) {
			mockedMapper.when(() -> DTOMapper.toPolicyDTO(policy)).thenReturn(policyDTO);

			PolicyDTO result = policyService.findByPolicyId(policyId);

			assertNotNull(result);
			assertEquals(policyId, result.getPolicyId());
		}
	}

	@Test
	void testFindByPolicyId_policyNotFound() {
		when(policyRepository.findByPolicyId(policyId)).thenReturn(Optional.empty());

		PolicyServiceException ex = assertThrows(PolicyServiceException.class,
				() -> policyService.findByPolicyId(policyId));

		assertTrue(ex.getMessage().contains("An error occurred while searching fot the policy by policy id"));
	}

	@Test
	void testFindByPolicyId_repositoryThrowsException() {
		when(policyRepository.findByPolicyId(policyId)).thenThrow(new RuntimeException("DB error"));

		PolicyServiceException ex = assertThrows(PolicyServiceException.class,
				() -> policyService.findByPolicyId(policyId));

		assertTrue(ex.getMessage().contains("An error occurred while searching fot the policy by policy id"));
	}
}
