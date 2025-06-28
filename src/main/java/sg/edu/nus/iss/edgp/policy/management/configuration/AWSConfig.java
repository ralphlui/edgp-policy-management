package sg.edu.nus.iss.edgp.policy.management.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class AWSConfig {
	@Value("${spring.cloud.aws.region.static}")
	private String awsRegion;

	@Value("${spring.cloud.aws.credentials.access-key}")
	private String awsAccessKey;

	@Value("${spring.cloud.aws.credentials.secret-key}")
	private String awsSecretKey;

	@Bean
	public String getAwsRegion() {
		return awsRegion;
	}

	@Bean
	public String getAwsAccessKey() {
		return awsAccessKey;
	}

	@Bean
	public String getAwsSecretKey() {
		return awsSecretKey;
	}

	@Bean
	public SqsClient sqsClient() {
		return SqsClient.builder().region(Region.AP_SOUTHEAST_1).build();
	}

}
