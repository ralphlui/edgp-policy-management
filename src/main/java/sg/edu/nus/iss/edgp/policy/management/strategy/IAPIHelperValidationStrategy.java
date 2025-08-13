package sg.edu.nus.iss.edgp.policy.management.strategy;

import sg.edu.nus.iss.edgp.policy.management.dto.ValidationResult;

public interface IAPIHelperValidationStrategy<T> {
	
	ValidationResult validateCreation(T data, String firstInput, String secondInput);
	
	ValidationResult validateUpdating(T data);
}
