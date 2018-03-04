# Business Transaction Detector

Reads Jaeger traces from a datasource, analyzes them in order to find all business transactions and attaches the found 
business transaction as a tag to the traces. 
The tag is called `business_transaction` and can be found in the `tags` field within the trace.

## Run with Gradle 
Example of how to run the application with gradle:

`./gradlew run -PappArgs="['src/main/resources/config/application.yml']"`

## Run as Jar

To build the fat jar run the following gradle command:

`./gradlew fatJar `

The fat jar can now be found in the `build/libs` directory.
Run the fat jar application with:

`java -jar rocks.inspectit.jaeger.dw-all-1.0.jar -h localhost -k jaeger_v1_test -s AppFin -d elasticsearch`

## Application Properties

Available configuration properties in the application.yml:
```
kafka:
  bootstrapServers: localhost:9092
  groupId: businessTransactionAnalyzer
  inputTopic: traces
  outputTopic: businessTraces

elasticsearch:
  host: localhost
  doc: jaeger-span-2017-12-09
  port: 9200
  scheme: html

cassandra:
 host: localhost
 keyspace: traces

database: kafka
serviceName: AppFin

startTime: 1512833158
endTime: 1512933158
interval: 1
```