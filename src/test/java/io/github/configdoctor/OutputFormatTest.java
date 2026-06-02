package io.github.configdoctor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class OutputFormatTest {

    @Test
    void acceptsLowercaseNames() {
        assertEquals(OutputFormat.TEXT, OutputFormat.from("text"));
        assertEquals(OutputFormat.JSON, OutputFormat.from("json"));
        assertEquals(OutputFormat.SARIF, OutputFormat.from("sarif"));
    }

    @Test
    void rejectsUnknownNames() {
        assertThrows(IllegalArgumentException.class, () -> OutputFormat.from("xml"));
    }
}
