package sg.edu.nus.iss.edgp.policy.management.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import sg.edu.nus.iss.edgp.policy.management.dto.PolicyDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.Rule;
import sg.edu.nus.iss.edgp.policy.management.entity.AppliedRule;
import sg.edu.nus.iss.edgp.policy.management.entity.Policy;

@Component
public class DTOMapper {

	private static final Pattern COMMA = Pattern.compile("\\s*,\\s*");

	public static PolicyDTO toPolicyDTO(Policy policy) {
		PolicyDTO policyDTO = new PolicyDTO();
		policyDTO.setPolicyId(policy.getPolicyId());
		policyDTO.setPolicyName(policy.getPolicyName());
		policyDTO.setDomainName(policy.getDomainName());
		policyDTO.setDescription(policy.getDescription());
		policyDTO.setPublished(policy.isPublished());
		policyDTO.setCreatedBy(policy.getCreatedBy());
		policyDTO.setLastUpdatedBy(policy.getLastUpdatedBy());

		List<AppliedRule> appliedRules = Optional.ofNullable(policy.getAppliedRules()).orElse(Collections.emptyList());
		List<Rule> rules = toRule(appliedRules);

		Optional.ofNullable(rules).filter(r -> r != null && !r.isEmpty()).ifPresent(r -> {
			policyDTO.setRules(r);
		});

		policyDTO.setOrganizationId(policy.getOrganizationId());
		return policyDTO;
	}

	private static List<Rule> toRule(List<AppliedRule> appliedRules) {
		List<Rule> rules = new ArrayList<>();

		for (AppliedRule ar : appliedRules) {
			if (ar == null)
				continue;

			Rule rule = new Rule();
			boolean populated = false;

			if (GeneralUtility.hasText(ar.getAppliesToField())) {
				List<String> fields = Arrays.stream(COMMA.split(ar.getAppliesToField())).filter(f -> !f.isEmpty())
						.collect(Collectors.toList());
				rule.setAppliesToField(fields);
				populated = true;
			}

			if (GeneralUtility.hasText(ar.getDescription())) {
				rule.setDescription(ar.getDescription().trim());
				populated = true;
			}
			
			if (GeneralUtility.hasText(ar.getRuleId())) {
				rule.setRuleId(ar.getRuleId().trim());
				populated = true;
			}

			Map<String, Object> params = ar.getParameters();
			if (params != null && !params.isEmpty()) {
				rule.setParameters(params);
				populated = true;
			}

			if (GeneralUtility.hasText(ar.getRuleName())) {
				rule.setRuleName(ar.getRuleName().trim());
				populated = true;
			}

			if (populated) {
				rules.add(rule);
			}
		}

		return rules;
	}
}
