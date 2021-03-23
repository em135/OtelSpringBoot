#!/bin/bash
if [ ! -f opentelemetry-javaagent-all.jar ]; then
    echo "Java agent not found, downloading version 1.0.1"
    curl -L -O https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent-all.jar
fi
mvn test -fae
