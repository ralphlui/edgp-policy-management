package sg.edu.nus.iss.edgp.policy.management.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Rule {
	private String ruleName;
	private List<String> appliesToField = new ArrayList<>();
	private String description = "";
	private Map<String, Object> parameters;

}
