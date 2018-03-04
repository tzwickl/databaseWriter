package rocks.inspectit.jaeger.dw.analyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.inspectit.jaeger.model.trace.elasticsearch.ExtendedTrace;
import rocks.inspectit.jaeger.model.trace.kafka.Trace;
import rocks.inspectit.jaeger.model.trace.kafka.TraceKeyValue;

import java.io.IOException;
import java.util.*;

import static rocks.inspectit.jaeger.model.Constants.BT_TAG;

public class TracesAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(TracesAnalyzer.class);
    final Map<String, Trace> spans;
    private final List<Trace> traces;

    public TracesAnalyzer(final List<Trace> traces) {
        this.spans = new HashMap<>();
        this.traces = traces;
    }

    public List<ExtendedTrace> transformTraces() {
        final List<ExtendedTrace> extendedTraces = new ArrayList<ExtendedTrace>();

        this.traces.forEach(trace -> {
            spans.put(trace.getSpanId(), trace);
            try {
                extendedTraces.add(this.transformToFlatTrace(trace));
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
            }
        });

        return extendedTraces;
    }

    private ExtendedTrace transformToFlatTrace(Trace trace) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ExtendedTrace extendedTrace = objectMapper.readValue(objectMapper.writeValueAsString(trace), ExtendedTrace.class);
        TraceKeyValue businessTransactionName = getBusinessTransactionName(trace);
        if (businessTransactionName != null) {
            extendedTrace.setBusinessTransactionName(businessTransactionName.getValue());
        }
        return extendedTrace;
    }

    private TraceKeyValue getBusinessTransactionName(Trace trace) {
        return trace.getTags().stream().filter(tag -> tag.getKey().equals(BT_TAG))
                .findFirst().orElse(null);
    }

    public Long getLatestTimestamp() {
        return this.traces.stream().max(Comparator.comparing(Trace::getStartTime)).get().getStartTime();
    }
}
