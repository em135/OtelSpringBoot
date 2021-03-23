package dk.sdu.mmmi;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

import static io.opentelemetry.api.GlobalOpenTelemetry.getPropagators;

@Component
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

	private final Tracer tracer = GlobalOpenTelemetry.getTracer("app");
	private final TextMapSetter<HttpRequest> setter = (carrier, key, value) ->
			Objects.requireNonNull(carrier).getHeaders().set(key, value);

	@Override
	public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
		SpanBuilder spanBuilder = tracer.spanBuilder("span");
		Span currentSpan = spanBuilder.setSpanKind(SpanKind.CLIENT).startSpan();

		try (Scope ignored = currentSpan.makeCurrent()) {
			getPropagators().getTextMapPropagator().inject(Context.current(), httpRequest, setter);
			return clientHttpRequestExecution.execute(httpRequest, bytes);
		} finally {
			currentSpan.end();
		}
	}
}
