package sg.edu.nus.iss.edgp.policy.management.connector;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import sg.edu.nus.iss.edgp.policy.management.dto.SearchRequest;

@Service
public class RuleAPICall {
	@Value("${rule.api.url}")
	private String ruleAPIUrl;

	private static final Logger logger = LoggerFactory.getLogger(AdminAPICall.class);

	public String retrieveGreatExpectationRuleList(String authorizationHeader, SearchRequest searchRequest) {
		logger.info("retrieving great expectation rule api is calling ..");
		String responseStr = "";

		try {
			HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();

			String url = ruleAPIUrl.trim();
			logger.info(url);

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(30))
					.header("Authorization", authorizationHeader).header("Content-Type", "application/json").GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			responseStr = response.body();

			logger.info("Retrieving all great expectation rule list response.");

		} catch (Exception e) {
			logger.error("An error occurred while fetching all great expectation rule list ", e);
		}
		return responseStr;
//		return "{\"success\":true,\"message\":\"Retrieved successfully\",\"totalRecord\": 3,\"data\":[{\"ruleId\":\"3695f5cb-9d5b-4faf-98d0-cdd6ec79975b\", \"ruleName\":\"expect_column_values_to_not_be_null\"},{ \"ruleId\":\"3695f5cb-9d5b-4faf-98d0-cdd6ec79975c\",\"ruleName\": \"expect_column_values_to_be_between\"},{\"ruleId\":\"3695f5cb-9d5b-4faf-98d0-cdd6ec79975d\",\"ruleName\":\"expect_table_row_count_to_be_between\"}]}";

	}
}
