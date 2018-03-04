# Database Writer

Reads Jaeger traces from a datasource, transforms them and writes them back to a different datasource.

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
  groupId: databaseWriter
  inputTopic: businessTraces
  outputTopic: outputTraces

elasticsearch:
  host: localhost
  doc: traces
  port: 9200
  scheme: html

cassandra:
 host: localhost
 keyspace: traces

input: kafka
output: elasticsearch
serviceName: AppFin

startTime: 1512833158
endTime: 1512933158
interval: 1
```