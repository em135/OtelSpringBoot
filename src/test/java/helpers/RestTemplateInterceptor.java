package helpers;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static io.opentelemetry.api.GlobalOpenTelemetry.getPropagators;

@Component
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private Tracer tracer = GlobalOpenTelemetry.getTracer("tracerName");

    private static final TextMapSetter<HttpRequest> setter = (carrier, key, value) -> carrier.getHeaders().set(key, value);

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
        HttpHeaders headers = httpRequest.getHeaders();


        SpanBuilder spanBuilder = tracer.spanBuilder("helloSpan");
        Span currentSpan = spanBuilder.setSpanKind(SpanKind.CLIENT).startSpan();

        try (Scope scope = currentSpan.makeCurrent()) {
            getPropagators().getTextMapPropagator().inject(Context.current(), httpRequest, setter);
            HttpHeaders headers1 = httpRequest.getHeaders();
            System.out.println("HEADERS_AFTER" + headers1);
            ClientHttpResponse response = clientHttpRequestExecution.execute(httpRequest, bytes);
            return response;
        } finally {
            currentSpan.end();
        }
    }
}
