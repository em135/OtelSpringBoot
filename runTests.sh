#!/bin/bash
if [ ! -f header-inject-auto-1.0.1/opentelemetry-javaagent-all.jar ]; then
    echo "New java agent not found, downloading version 1.0.1"
    curl -L -o header-inject-auto-1.0.1/opentelemetry-javaagent-all.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent-all.jar
fi

if [ ! -f header-inject-auto-0.16.0/opentelemetry-javaagent-all.jar ]; then
    echo "Old java agent not found, downloading version 0.16.0"
    curl -L -o header-inject-auto-0.16.0/opentelemetry-javaagent-all.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v0.16.0/opentelemetry-javaagent-all.jar
fi

mvn test -fae
