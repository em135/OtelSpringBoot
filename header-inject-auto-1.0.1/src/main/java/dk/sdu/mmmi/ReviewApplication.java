package dk.sdu.mmmi;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@SpringBootApplication
//@EnableOpenTelemetry
public class ReviewApplication {

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
}
