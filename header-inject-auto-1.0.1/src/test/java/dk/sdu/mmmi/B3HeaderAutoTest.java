package dk.sdu.mmmi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
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
import java.util.Collections;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = ReviewApplication.class)
public class B3HeaderAutoTest {

	@Autowired
	private RestTemplate restTemplate;
	private HttpHeaders httpHeaders;

	@BeforeEach
	public void setUp() throws IOException {
		// Local reference for asserting the headers that have been set
		httpHeaders = new HttpHeaders();

		// Mocking out the request pipeline
		MockClientHttpRequest httpRequest = Mockito.spy(new MockClientHttpRequest());
		httpRequest.setResponse(new MockClientHttpResponse(new byte[0], HttpStatus.NO_CONTENT));
		Mockito.lenient().when(httpRequest.getHeaders()).thenReturn(httpHeaders);

		ClientHttpRequestFactory requestFactory = Mockito.mock(ClientHttpRequestFactory.class);
		Mockito.lenient().when(requestFactory.createRequest(Mockito.any(), Mockito.any())).thenReturn(httpRequest);

		// Instruct the RestTemplate to use our mock
		restTemplate.setRequestFactory(requestFactory);
	}

	/**
	 * Asserts that the B3 headers have been injected into the request headers.
	 */
	private void assertB3Headers() {
		System.out.println(">>>  " + httpHeaders);
		Arrays.asList(
				"X-B3-TraceId",
				"X-B3-SpanId",
				"X-B3-Sampled"
		).forEach(header -> Assertions.assertNotNull(
				httpHeaders.get(header),
				"Missing header " + header
		));
	}

	@Test
	public void globalTracer_injectsHeaders() {
		// Arrange
		restTemplate.setInterceptors(Collections.singletonList(new CustomInterceptor()));

		// Act
		restTemplate.getForObject("http://localhost:8080", String.class);

		// Assert that only our interceptor is available
		Assertions.assertEquals(
				restTemplate.getInterceptors().size(), 1,
				"Too many or too few interceptors available"
		);
		Assertions.assertTrue(
				restTemplate.getInterceptors().get(0) instanceof dk.sdu.mmmi.CustomInterceptor,
				"The custom interceptor was not found"
		);

		// Assert that our own interceptor, based on the Global tracer, injects B3 headers
		assertB3Headers();
	}

	@Test
	public void autoInterceptor_isInjected() {
		// Assert that the interceptor from "OpenTelemetry Spring Auto-Configuration" is available
		Assertions.assertEquals(
				restTemplate.getInterceptors().size(), 1,
				"Too many or too few interceptors available"
		);
		Assertions.assertTrue(
				restTemplate.getInterceptors().get(0) instanceof io.opentelemetry.instrumentation.spring.httpclients.RestTemplateInterceptor,
				"The auto-configure interceptor was not found"
		);
	}

	@Test
	public void autoInterceptor_injectsHeaders() {
		// Act
		restTemplate.getForObject("http://localhost:8080", String.class);

		// Assert that the interceptor from "OpenTelemetry Spring Auto-Configuration" injects B3 headers
		assertB3Headers();
	}
}
