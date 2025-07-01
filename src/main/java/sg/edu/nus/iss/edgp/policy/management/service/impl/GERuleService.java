package sg.edu.nus.iss.edgp.policy.management.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.policy.management.connector.RuleAPICall;
import sg.edu.nus.iss.edgp.policy.management.dto.GERuleDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.SearchRequest;
import sg.edu.nus.iss.edgp.policy.management.exception.GERuleServiceException;
import sg.edu.nus.iss.edgp.policy.management.service.IGERuleService;
import sg.edu.nus.iss.edgp.policy.management.utility.GeneralUtility;
import sg.edu.nus.iss.edgp.policy.management.utility.JSONReader;

@Service
@RequiredArgsConstructor
public class GERuleService implements IGERuleService {

	private final RuleAPICall ruleAPICall;
	private final JSONReader jsonReader;
	private static final Logger logger = LoggerFactory.getLogger(GERuleService.class);

	@Override
	public Map<Long, List<GERuleDTO>> retrieveGERuleList(String authHeader, SearchRequest searchRequest) {
		String responseStr = ruleAPICall.retrieveGreatExpectationRuleList(authHeader, searchRequest);

		try {
			JSONParser parser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);
			if (!jsonReader.getSuccessFromResponse(jsonResponse)) {
				return null;
			}

			JSONArray dataList = jsonReader.getDataArrayFromResponse(jsonResponse);
			Map<Long, List<GERuleDTO>> result = new HashMap<>();

			if (dataList != null && !dataList.isEmpty()) {
				List<GERuleDTO> geRuleDTOList = new ArrayList<>();

				for (Object item : dataList) {
					JSONObject data = (JSONObject) item;
					GERuleDTO geRuleDTO = new GERuleDTO();

					String ruleId = GeneralUtility.makeNotNull(String.valueOf(data.get("ruleId")));
					String ruleName = GeneralUtility.makeNotNull(String.valueOf(data.get("ruleName")));

					geRuleDTO.setRuleId(ruleId);
					geRuleDTO.setRuleName(ruleName);
					geRuleDTOList.add(geRuleDTO);
				}

				result.put((long) dataList.size(), geRuleDTOList);
			}

			return result;

		} catch (ParseException e) {
			logger.error("Error parsing JSON response for retrieving GE rule list...", e);
			throw new GERuleServiceException("An error occurred while retrieving GE rule list", e);
		}
	}

}
