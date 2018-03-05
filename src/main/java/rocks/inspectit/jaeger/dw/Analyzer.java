package rocks.inspectit.jaeger.dw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.inspectit.jaeger.connectors.IDatasource;
import rocks.inspectit.jaeger.connectors.cassandra.Cassandra;
import rocks.inspectit.jaeger.connectors.elasticsearch.Elasticsearch;
import rocks.inspectit.jaeger.connectors.kafka.Kafka;
import rocks.inspectit.jaeger.dw.analyzer.TracesAnalyzer;
import rocks.inspectit.jaeger.model.config.Configuration;
import rocks.inspectit.jaeger.model.trace.elasticsearch.ExtendedTrace;
import rocks.inspectit.jaeger.model.trace.kafka.Trace;

import java.util.List;

import static rocks.inspectit.jaeger.model.config.CassandraConfig.CASSANDRA;
import static rocks.inspectit.jaeger.model.config.ElasticSearchConfig.ELASTICSEARCH;
import static rocks.inspectit.jaeger.model.config.KafkaConfig.KAFKA;

public class Analyzer {
    private static final Logger logger = LoggerFactory.getLogger(Analyzer.class);
    private final Configuration configuration;

    public Analyzer(Configuration configuration) {
        this.configuration = configuration;
    }

    public int start() {
        IDatasource input = null;

        switch (configuration.getInput()) {
            case CASSANDRA:
                input = new Cassandra(configuration.getCassandra());
                logger.info("Using " + CASSANDRA + " for input");
                break;
            case ELASTICSEARCH:
                input = new Elasticsearch(configuration.getElasticsearch());
                logger.info("Using " + ELASTICSEARCH + " for input");
                break;
            case KAFKA:
                input = new Kafka(configuration.getKafka());
                logger.info("Using " + KAFKA + " for input");
                break;
            default:
                logger.error(configuration.getInput() + " is not a known database!");
                return 1;
        }

        IDatasource output = null;

        switch (configuration.getOutput()) {
            case CASSANDRA:
                output = new Cassandra(configuration.getCassandra());
                logger.info("Using " + CASSANDRA + " for output");
                break;
            case ELASTICSEARCH:
                output = new Elasticsearch(configuration.getElasticsearch());
                logger.info("Using " + ELASTICSEARCH + " for output");
                break;
            case KAFKA:
                output = new Kafka(configuration.getKafka());
                logger.info("Using " + KAFKA + " for output");
                break;
            default:
                logger.error(configuration.getOutput() + " is not a known datasource!");
                return 1;
        }

        Long startTime = null;
        Long endTime = null;
        try {
            startTime = configuration.getStartTime() * 1000000;
            endTime = configuration.getEndTime() * 1000000;
        } catch (NumberFormatException e) {
            // Do nothing
        }

        Long follow = null;
        try {
            follow = configuration.getInterval() * 1000;
        } catch (NumberFormatException e) {
            // Do nothing
        }

        if (follow == null) {
            run(input, output, configuration.getServiceName(), startTime, endTime);
        } else {
            if (startTime == null) {
                startTime = System.currentTimeMillis() * 1000;
                logger.info("Timestamp: " + startTime);
            }
            try {
                while (true) {
                    startTime = run(input, output, configuration.getServiceName(), startTime, null) + 1;
                    Thread.sleep(follow);
                }
            } catch (InterruptedException e) {
                // Do nothing
            }
        }

        return 0;
    }

    private Long run(IDatasource input, IDatasource output, String serviceName, Long startTime, Long endTime) {
        logger.info("###############_START_###############");
        List<Trace> traces;

        if (startTime != null && endTime != null) {
            traces = input.getTraces(serviceName, startTime, endTime);
        } else if (startTime != null) {
            traces = input.getTraces(serviceName, startTime);
        } else {
            traces = input.getTraces(serviceName);
        }

        logger.info("Number of Traces to analyze: " + traces.size());

        if (traces.isEmpty()) {
            logger.warn("No Traces found to analyze!");
            logger.info("###############_END_#################");
            return startTime;
        }

        TracesAnalyzer tracesAnalyzer = new TracesAnalyzer(traces);
        List<ExtendedTrace> extendedTraces = tracesAnalyzer.transformTraces();

        logger.info("Finished analyzing Traces");

        output.saveTraces(extendedTraces);

        logger.info("Updated Traces in database");
        logger.info("###############_END_#################");

        return tracesAnalyzer.getLatestTimestamp();
    }
}
