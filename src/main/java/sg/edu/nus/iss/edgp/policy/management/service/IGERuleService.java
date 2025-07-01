package sg.edu.nus.iss.edgp.policy.management.service;

import java.util.List;
import java.util.Map;

import sg.edu.nus.iss.edgp.policy.management.dto.GERuleDTO;
import sg.edu.nus.iss.edgp.policy.management.dto.SearchRequest;

public interface IGERuleService {

	 Map<Long, List<GERuleDTO>> retrieveGERuleList(String authHeader, SearchRequest searchRequest);
}
