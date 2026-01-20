package es.ull.simulation.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class OutputTest {

    @Test
    void shouldWriteErrorRegardlessOfDebugFlag() {
        ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
        Output output = new Output(false,
                new OutputStreamWriter(new ByteArrayOutputStream(), StandardCharsets.UTF_8),
                new OutputStreamWriter(errBytes, StandardCharsets.UTF_8));

        output.error("boom");

        String err = errBytes.toString(StandardCharsets.UTF_8);
        assertTrue(err.contains("ERROR!\tboom"));
    }

    @Test
    void shouldWriteDebugOnlyWhenEnabled() {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        Output output = new Output(false, new OutputStreamWriter(outBytes, StandardCharsets.UTF_8));

        output.debug("hidden");
        assertEquals("", outBytes.toString(StandardCharsets.UTF_8));

        output.setDebugEnabled(true);
        output.debug("visible");
        assertTrue(outBytes.toString(StandardCharsets.UTF_8).contains("visible"));
    }
}
