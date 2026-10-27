package org.example.redpic;

import org.example.redpic.config.AiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AiProperties.class)
public class RedPicApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedPicApplication.class, args);
	}

}
