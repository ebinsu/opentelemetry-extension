FROM busybox:1.34.1
COPY /build/libs/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar
VOLUME ["/otel-javaagent"]
CMD ["cp", "/opentelemetry-javaagent.jar", "/otel-javaagent/opentelemetry-javaagent.jar"]