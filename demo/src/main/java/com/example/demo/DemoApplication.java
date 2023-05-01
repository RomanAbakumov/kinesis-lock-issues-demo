package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;

import java.net.URI;
import java.util.function.Consumer;

@EnableScheduling
@SpringBootApplication
public class DemoApplication {
	public static final URI ENDPOINT_OVERRIDE = URI.create("http://localhost:4566");
	@Autowired
	private StreamBridge streamBridge;
	int messageId = 0;
	
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Scheduled(fixedDelay = 10000L)
	public void dummyLoad() {
		String messageBody = "Message: " + messageId++;
		streamBridge.send("messages2-out-0", "kinesis", messageBody);
		System.out.println("Message sent: " + messageBody);
	}
	
	@Bean
	public Consumer<String> messages() {
		return message -> System.out.println("Received message: " + message);
	}

	@Bean
	public AwsCredentialsProvider awsCredentialsProvider() {
		return () -> AwsBasicCredentials.create("test", "test");
	}
	@Bean
	public KinesisAsyncClient amazonKinesis(AwsCredentialsProvider awsCredentialsProvider) {
		return KinesisAsyncClient.builder()
				.httpClient(NettyNioAsyncHttpClient.create())
				.credentialsProvider(awsCredentialsProvider)
				.endpointOverride(ENDPOINT_OVERRIDE)
				.region(Region.US_EAST_1)
				.build();
	}
	@Bean
	public DynamoDbClient dynamoDbClient(AwsCredentialsProvider awsCredentialsProvider) {
		return DynamoDbClient.builder()
				.httpClient(UrlConnectionHttpClient.create())
				.credentialsProvider(awsCredentialsProvider)
				.endpointOverride(ENDPOINT_OVERRIDE)
				.region(Region.US_EAST_1)
				.build();
	}
	@Bean
	public DynamoDbAsyncClient dynamoDbAsyncClient(AwsCredentialsProvider awsCredentialsProvider) {
		return DynamoDbAsyncClient.builder()
//				.httpClient(NettyNioAsyncHttpClient.builder()
//						.connectionTimeout(Duration.ofSeconds(30))
//						.build())
				.credentialsProvider(awsCredentialsProvider)
				.endpointOverride(ENDPOINT_OVERRIDE)
				.region(Region.US_EAST_1)
				.build();
	}

	@Bean
	public Consumer<ErrorMessage> errorHandler() {
		return v -> System.out.println("Can't process message: " + v.getOriginalMessage());
	}
}
