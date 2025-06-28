package sg.edu.nus.iss.edgp.policy.management.service.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.policy.management.aws.service.SQSPublishingService;
import sg.edu.nus.iss.edgp.policy.management.dto.AuditDTO;
import sg.edu.nus.iss.edgp.policy.management.enums.AuditResponseStatus;
import sg.edu.nus.iss.edgp.policy.management.service.IAuditService;

@Service
@RequiredArgsConstructor
public class AuditService implements IAuditService {

	@Value("${audit.activity.type.prefix}")
	String activityTypePrefix;

	private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

	private final SQSPublishingService sqsPublishingService;
	private final JwtService jwtService;

	public void logAudit(AuditDTO auditDTO, int stausCode, String message, String authorizationHeader) {
		auditDTO.setStatusCode(stausCode);
		if (stausCode == 200) {
			auditDTO.setResponseStatus(AuditResponseStatus.SUCCESS);

		} else {
			auditDTO.setResponseStatus(AuditResponseStatus.FAILED);
		}
		auditDTO.setActivityDescription(message);
		this.sendMessage(auditDTO, authorizationHeader);

	}

	public AuditDTO createAuditDTO(String activityType, String endpoint, String httpVerb) {
		AuditDTO auditDTO = new AuditDTO();
		auditDTO.setActivityType(activityTypePrefix.trim() + activityType);
		auditDTO.setRequestHTTPVerb(httpVerb);
		auditDTO.setRequestActionEndpoint(endpoint);
		return auditDTO;
	}

	@Override
	public void sendMessage(AuditDTO autAuditDTO, String authorizationHeader) {
		try {
			String jwtToken = "";
			String userName = "Invalid Username";
			String userId = "Invalid UserId";
			if (authorizationHeader.length() > 0) {
				jwtToken = authorizationHeader.substring(7);
			}

			if (!jwtToken.isEmpty()) {
				userName = Optional.ofNullable(jwtService.extractUserNameFromToken(jwtToken))
						.orElse("Invalid Username");
				userId = Optional.ofNullable(jwtService.extractUserIdFromToken(jwtToken)).orElse("Invalid UserId");
				autAuditDTO.setUsername(userName);
				autAuditDTO.setUserId(userId);

			}
			autAuditDTO.setUsername(userName);
			sqsPublishingService.sendMessage(autAuditDTO);

		} catch (Exception e) {

			logger.error("Error sending generateMessage to SQS: {}", e);
		}

	}
}
