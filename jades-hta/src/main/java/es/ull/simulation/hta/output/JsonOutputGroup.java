package es.ull.simulation.hta.output;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public final class JsonOutputGroup {
    private static final Phase DEFAULT_PHASE = Phase.ALL;
    private static final Processing DEFAULT_PROCESSING = Processing.JSON;

    public enum Phase {
        BASE_CASE("base_case"),
        PSA_RUNS("psa_runs"),
        ALL("all");

        private final String wire;
        Phase(String wire) { this.wire = wire; }

        @com.fasterxml.jackson.annotation.JsonValue
        public String toWire() { return wire; }

        @com.fasterxml.jackson.annotation.JsonCreator
        public static Phase fromWire(String v) {
            for (Phase p : values()) if (p.wire.equals(v)) return p;
            throw new IllegalArgumentException("Unknown phase: " + v);
        }
    }

    public enum Processing {
        JSON("json"),
        CSV("csv"),
        TXT("txt");

        private final String wire;
        Processing(String wire) { this.wire = wire; }

        @com.fasterxml.jackson.annotation.JsonValue
        public String toWire() { return wire; }

        @com.fasterxml.jackson.annotation.JsonCreator
        public static Processing fromWire(String v) {
            for (Processing p : values()) if (p.wire.equals(v)) return p;
            throw new IllegalArgumentException("Unknown processing: " + v);
        }
    }

    private final Phase phase;
    private final Processing processing;
    private final String file;
    private final List<JsonOutputSpec> list;

    @JsonCreator
    public JsonOutputGroup(
            @JsonProperty("phase") Phase phase,
            @JsonProperty("processing") Processing processing,
            @JsonProperty("file") String file,
            @JsonProperty(value = "list", required = true) List<JsonOutputSpec> list
    ) {
        this.phase = phase != null ? phase : DEFAULT_PHASE;
        this.processing = processing != null ? processing : DEFAULT_PROCESSING;
        this.file = file; // optional
        this.list = List.copyOf(list);
    }

    public Phase getPhase() { return phase; }
    public Processing getProcessing() { return processing; }
    public String getFile() { return file; }
    public List<JsonOutputSpec> getList() { return list; }
}
