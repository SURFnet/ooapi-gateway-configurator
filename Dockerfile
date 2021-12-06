FROM clojure:lein as builder
RUN mkdir /app
WORKDIR /app
COPY . /app/
RUN lein uberjar

FROM gcr.io/distroless/java:11
COPY --from=builder /app/target/uberjar/ooapi-gateway-configurator.jar /ooapi-gateway-configurator.jar
ENV TZ=CET
COPY configurator.production.logback.xml /
ENV JDK_JAVA_OPTIONS="-Dlogback.configurationFile=configurator.production.logback.xml"
CMD ["ooapi-gateway-configurator.jar"]
