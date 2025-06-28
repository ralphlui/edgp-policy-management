package sg.edu.nus.iss.edgp.policy.management.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import sg.edu.nus.iss.edgp.policy.management.entity.Rule;

@Getter
@Setter
public class PolicyDTO {

	private String policyId;
	private String policyName;
	private String description;
	private String domainName;
	private boolean isPublished;
	private List<Rule> rules = new ArrayList<>();
	private String createdBy;
	private String lastUpdatedBy;
	private String organizationId;

}
