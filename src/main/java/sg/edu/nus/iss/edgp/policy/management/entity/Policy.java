package sg.edu.nus.iss.edgp.policy.management.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Policy {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.AUTO)
	private String policyId;
	
	@Column(nullable = false, unique = true)
	private String policyName;
	
	@Column(nullable = true)
	private String description;
	
	@Column(nullable = false)
	private String domainName;
	
	@Column(nullable = false, columnDefinition = "boolean default true")
	private boolean isPublished = true;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "policyId", referencedColumnName = "policyId")
	private List<Rule> rules = new ArrayList<>();

	@Column(nullable = false, columnDefinition = "datetime")
	private LocalDateTime createdDateTime = LocalDateTime.now();

	@Column(nullable = false)
	private String createdBy;

	@Column(nullable = false, columnDefinition = "datetime")
	private LocalDateTime lastUpdatedDateTime = LocalDateTime.now();;

	@Column(nullable = false)
	private String lastUpdatedBy;
	
	@Column(nullable = false)
	private String organizationId;
	
	
}
