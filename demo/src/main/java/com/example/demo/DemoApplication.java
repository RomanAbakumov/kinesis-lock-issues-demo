package com.example.demo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Collections;
import java.util.function.Consumer;

@EnableScheduling
@SpringBootApplication
public class DemoApplication {
	@Autowired
	private StreamBridge streamBridge;
	int messageId = 0;
	
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Scheduled(fixedDelay = 10000L)
	public void dummyLoad() {
		String messageBody = "Message: " + messageId++;
		Message<String> message = MessageBuilder.createMessage(messageBody, new MessageHeaders(Collections.singletonMap("spring.cloud.function.definition", "asd")));
		streamBridge.send("messages-out-0", "kinesis", message);
		System.out.println("Message sent: " + messageBody);
	}
	
	@Bean
	public Consumer<Message<String>> messages() {
		return message -> System.out.println("Received message: " + message.getPayload());
	}


	@Bean
	public AWSCredentialsProvider awsCredentialsProvider() {
		return new AWSCredentialsProvider() {
			@Override
			public AWSCredentials getCredentials() {
				return new AWSCredentials() {
					@Override
					public String getAWSAccessKeyId() {
						return "test";
					}

					@Override
					public String getAWSSecretKey() {
						return "test";
					}
				};
			}

			@Override
			public void refresh() {
			}
		};
	}

	@Bean
	public AmazonKinesisAsync amazonKinesis(AWSCredentialsProvider awsCredentialsProvider) {
		return AmazonKinesisAsyncClientBuilder
				.standard()
				.withCredentials(awsCredentialsProvider)
				.withEndpointConfiguration(getKinesisEndpointConfiguration())
				.build();
	}

	@Bean
	public AmazonKinesis amazonKinesisClient(AWSCredentialsProvider awsCredentialsProvider) {
		return AmazonKinesisClientBuilder
				.standard()
				.withCredentials(awsCredentialsProvider)
				.withEndpointConfiguration(getKinesisEndpointConfiguration())
				.build();
	}

	@Bean
	public AmazonDynamoDBAsync amazonDynamoDBAsync(AWSCredentialsProvider awsCredentialsProvider) {
		return AmazonDynamoDBAsyncClientBuilder.standard()
				.withCredentials(awsCredentialsProvider)
				.withEndpointConfiguration(getDynamoDbEndpointConfiguration())
				.build();
	}


	public AwsClientBuilder.EndpointConfiguration getKinesisEndpointConfiguration() {
		return new AwsClientBuilder.EndpointConfiguration(
				"http://localhost:4566",
				"us-east-1");
	}

	public AwsClientBuilder.EndpointConfiguration getDynamoDbEndpointConfiguration() {
		return new AwsClientBuilder.EndpointConfiguration(
				"http://localhost:4566",
				"us-east-1");
	}

}
