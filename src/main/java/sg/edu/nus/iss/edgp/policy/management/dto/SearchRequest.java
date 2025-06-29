package sg.edu.nus.iss.edgp.policy.management.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Min;

@Getter
@Setter
public class SearchRequest {

	@Min(1)
	private Integer page;

	@Min(1)
	private Integer size;
	
	private Boolean isPublished; 

}
