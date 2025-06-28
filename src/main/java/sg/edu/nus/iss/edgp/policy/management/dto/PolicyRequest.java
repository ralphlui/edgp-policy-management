package sg.edu.nus.iss.edgp.policy.management.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import sg.edu.nus.iss.edgp.policy.management.entity.Rule;

@Getter
@Setter
public class PolicyRequest {

	private String policyId;
	private String policyName;
	private String description;
	private String domainName;
	@JsonProperty("isPublished")
	private boolean isPublished = true;
	private List<Rule> rules = new ArrayList<>();
	private String createdBy;
	private String lastUpdatedBy;
}
