import com.sun.tools.javac.util.Assert;
import helpers.RestTemplateInterceptor;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
public class B3MultiTest {

    @Mock
    private ClientHttpRequestFactory requestFactory;

    @Spy
    private MockClientHttpRequest httpRequest = new MockClientHttpRequest();

    private HttpHeaders httpHeaders = new HttpHeaders();

    @Test
    public void test() throws IOException {
        SpanProcessor logProcessor = SimpleSpanProcessor.create(new LoggingSpanExporter());
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().addSpanProcessor(logProcessor).build();
        OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
        httpRequest.setResponse(new MockClientHttpResponse(new byte[0], HttpStatus.NO_CONTENT));
        Mockito.when(requestFactory.createRequest(Mockito.any(), Mockito.any())).thenReturn(httpRequest);
        Mockito.when(httpRequest.getHeaders()).thenReturn(httpHeaders);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateInterceptor()));

        restTemplate.getForObject("http://localhost:8080", String.class);

        Assert.checkNonNull(httpHeaders.get("X-B3-TraceId"));
        Assert.checkNonNull(httpHeaders.get("X-B3-SpanId"));
        Assert.checkNonNull(httpHeaders.get("X-B3-Sampled"));
    }

}
