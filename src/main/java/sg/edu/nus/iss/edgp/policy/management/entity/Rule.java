package sg.edu.nus.iss.edgp.policy.management.entity;

import java.util.Map;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;
import sg.edu.nus.iss.edgp.policy.management.utility.JsonConverter;

@Entity
@Getter
@Setter
public class Rule {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.AUTO)
	private String ruleId;
	
	@Column(nullable = false)
	private String ruleName;
	
	@Column(nullable = false)
	private String appliesToField;
	
	@Column(nullable = true)
	private String description;
	
	  @Lob
	  @Convert(converter = JsonConverter.class)
	  private Map<String, Object> parameters;

	
}
