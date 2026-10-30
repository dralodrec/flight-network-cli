package org.keyin.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConsolePrinterTest {

    private ConsolePrinter printer;
    private PrintStream originalOut;
    private PrintStream capturedOut;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setUp() {
        printer = new ConsolePrinter();

        originalOut = System.out;
        output = new ByteArrayOutputStream();
        capturedOut = new PrintStream(
                output, true, StandardCharsets.UTF_8
        );
        System.setOut(capturedOut);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        capturedOut.close();
    }

    @Test
    void printsTitleAndAllReportEntries() {
        printer.printReport(
                "Airports by City",
                Map.of(
                        "Toronto", List.of("YYZ", "YTZ"),
                        "Dubai", List.of("DXB")
                )
        );

        List<String> lines = text().lines().toList();

        assertTrue(lines.contains("Airports by City"));
        assertTrue(lines.contains("Toronto: YYZ, YTZ"));
        assertTrue(lines.contains("Dubai: DXB"));
        assertFalse(text().contains("No records found."));
    }

    @Test
    void emptyReportPrintsNoRecordsMessage() {
        printer.printReport("Empty Report", Map.of());

        assertTrue(text().contains("Empty Report"));
        assertTrue(text().contains("No records found."));
    }

    @Test
    void emptyValueListPrintsNoneListed() {
        printer.printReport(
                "Airports by City",
                Map.of("Toronto", List.of())
        );

        assertTrue(text().lines().toList()
                .contains("Toronto: None listed"));
    }

    @Test
    void nullValueListPrintsNoneListed() {
        // HashMap permits null values; Map.of does not.
        Map<String, List<String>> report = new HashMap<>();
        report.put("Toronto", null);

        printer.printReport("Airports by City", report);

        assertTrue(text().lines().toList()
                .contains("Toronto: None listed"));
    }

    private String text() {
        return output.toString(StandardCharsets.UTF_8);
    }
}