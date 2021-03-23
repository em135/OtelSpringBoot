package dk.sdu.mmmi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = ReviewApplication.class)
public class B3HeaderAutoTest {

	@Spy
	private final MockClientHttpRequest httpRequest = new MockClientHttpRequest();
	private final HttpHeaders httpHeaders = new HttpHeaders();
	@Autowired
	private RestTemplate restTemplate;
	@Mock
	private ClientHttpRequestFactory requestFactory;

	@Test
	public void interceptor_isInjected() {
		// Assert
		Assertions.assertFalse(
				restTemplate.getInterceptors().isEmpty(),
				"No interceptor was injected"
		);
		Assertions.assertTrue(
				restTemplate.getInterceptors().get(0) instanceof io.opentelemetry.instrumentation.spring.httpclients.RestTemplateInterceptor,
				"The injected interceptor is incorrect"
		);
	}

	@Test
	public void interceptor_injectsHeaders() throws IOException {
		// Arrange
		httpRequest.setResponse(new MockClientHttpResponse(new byte[0], HttpStatus.NO_CONTENT));
		Mockito.when(requestFactory.createRequest(Mockito.any(), Mockito.any())).thenReturn(httpRequest);
		Mockito.when(httpRequest.getHeaders()).thenReturn(httpHeaders);
		restTemplate.setRequestFactory(requestFactory);

		// Act
		restTemplate.getForObject("http://localhost:8080", String.class);

		// Assert
		Arrays.asList(
				"X-B3-TraceId",
				"X-B3-SpanId",
				"X-B3-Sampled"
		).forEach(header -> Assertions.assertNotNull(
				httpHeaders.get(header),
				"Missing header " + header
		));
	}
}
