package dk.sdu.mmmi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
public class B3HeaderManualTest {

	@Spy
	private final MockClientHttpRequest httpRequest = new MockClientHttpRequest();
	private final HttpHeaders httpHeaders = new HttpHeaders();
	@Mock
	private ClientHttpRequestFactory requestFactory;

	@Test
	public void test() throws IOException {
		// Arrange
		httpRequest.setResponse(new MockClientHttpResponse(new byte[0], HttpStatus.NO_CONTENT));
		Mockito.when(requestFactory.createRequest(Mockito.any(), Mockito.any())).thenReturn(httpRequest);
		Mockito.when(httpRequest.getHeaders()).thenReturn(httpHeaders);

		RestTemplate restTemplate = new RestTemplate(requestFactory);
		restTemplate.setInterceptors(Collections.singletonList(new RestTemplateInterceptor()));

		// Act
		restTemplate.getForObject("http://localhost:8080", String.class);

		// Assert
		Assertions.assertNotNull(httpHeaders.get("X-B3-TraceId"));
		Assertions.assertNotNull(httpHeaders.get("X-B3-SpanId"));
		Assertions.assertNotNull(httpHeaders.get("X-B3-Sampled"));
	}
}
