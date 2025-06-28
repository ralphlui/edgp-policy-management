package sg.edu.nus.iss.edgp.policy.management.service;

import sg.edu.nus.iss.edgp.policy.management.dto.AuditDTO;

public interface IAuditService {
	void sendMessage(AuditDTO autAuditDTO, String authorizationHeader);

}
