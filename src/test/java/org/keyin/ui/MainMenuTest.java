package org.keyin.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keyin.history.HistoryManager;
import org.keyin.history.ReportRecord;
import org.keyin.service.ReportService;
import org.mockito.MockMakers;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MainMenuTest {

    private ReportService service;
    private ConsolePrinter printer;
    private HistoryManager history;

    private PrintStream originalOut;
    private PrintStream capturedOut;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setUp() {
        service = mock(
                ReportService.class,
                withSettings().mockMaker(MockMakers.SUBCLASS)
        );
        printer = mock(
                ConsolePrinter.class,
                withSettings().mockMaker(MockMakers.SUBCLASS)
        );
        history = new HistoryManager();

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
    void zeroExitsWithoutProcessingLaterInput() {
        runMenu("0\n1\n");

        assertTrue(text().contains("Goodbye!"));
        assertTrue(history.getHistory().isEmpty());
        verifyNoInteractions(service, printer);
    }

    @Test
    void endOfInputExitsWithoutRequestingAReport() {
        runMenu("");

        assertTrue(history.getHistory().isEmpty());
        verifyNoInteractions(service, printer);
    }

    @Test
    void allFourChoicesDisplayAndRecordTheCorrectReports() {
        for (int number = 1; number <= 4; number++) {
            prepareReport(number);
        }

        runMenu("1\n2\n3\n4\n0\n");

        for (int number = 1; number <= 4; number++) {
            verify(service).getTitle(number);
            verify(service).getReport(number);
            verify(printer).printReport(
                    "Report " + number, reportFor(number)
            );
        }

        List<ReportRecord> records = history.getHistory();

        assertEquals(
                List.of(1, 2, 3, 4),
                records.stream()
                        .map(ReportRecord::questionNumber)
                        .toList()
        );
        assertEquals(
                List.of("Report 1", "Report 2", "Report 3", "Report 4"),
                records.stream().map(ReportRecord::title).toList()
        );

        verifyNoMoreInteractions(service, printer);
    }

    @Test
    void invalidInputAllowsAnotherChoiceAndWhitespaceIsTrimmed() {
        prepareReport(1);

        runMenu("hello\n9\n\n 1 \n0\n");

        long invalidMessages = text().lines()
                .filter(line -> line.contains(
                        "Invalid choice. Enter 0 to 6."
                ))
                .count();

        assertEquals(3, invalidMessages);
        verify(printer).printReport("Report 1", reportFor(1));
        assertEquals(1, history.getHistory().size());
        assertEquals(1, history.getHistory().getFirst().questionNumber());
    }

    @Test
    void historyChoiceExplainsWhenHistoryIsEmpty() {
        runMenu("5\n0\n");

        assertTrue(text().contains("No reports viewed yet."));
        assertTrue(history.getHistory().isEmpty());
        verifyNoInteractions(service, printer);
    }

    @Test
    void historyChoiceDisplaysRecordsInOrderWithoutAddingRecords() {
        history.addRecord(2, "Earlier report");
        history.addRecord(1, "Later report");

        runMenu("5\n0\n");

        String printed = text();
        int earlier = printed.indexOf("1. Earlier report | ");
        int later = printed.indexOf("2. Later report | ");

        assertTrue(printed.contains("Viewing History"));
        assertTrue(earlier >= 0);
        assertTrue(later > earlier);
        assertEquals(2, history.getHistory().size());
        verifyNoInteractions(service, printer);
    }

    @Test
    void previousRecordNeedsAtLeastTwoViewedReports() {
        history.addRecord(1, "Report 1");

        runMenu("6\n0\n");

        assertTrue(text().contains(
                "View at least two reports to see a previous record."
        ));
        assertEquals(1, history.getHistory().size());
        verifyNoInteractions(service, printer);
    }

    @Test
    void previousRecordDisplaysEarlierReportWithoutChangingHistory() {
        history.addRecord(1, "Report 1");
        history.addRecord(2, "Report 2");
        when(service.getReport(1)).thenReturn(reportFor(1));

        List<ReportRecord> before = history.getHistory();

        runMenu("6\n6\n0\n");

        verify(service, times(2)).getReport(1);
        verify(printer, times(2)).printReport(
                "Previous: Report 1", reportFor(1)
        );
        assertEquals(before, history.getHistory());
        verifyNoMoreInteractions(service, printer);
    }

    private void runMenu(String input) {
        try (Scanner scanner = new Scanner(input)) {
            MainMenu menu = new MainMenu(
                    service, printer, history, scanner
            );
            menu.start();
        }
    }

    private void prepareReport(int number) {
        when(service.getTitle(number))
                .thenReturn("Report " + number);
        when(service.getReport(number))
                .thenReturn(reportFor(number));
    }

    private Map<String, List<String>> reportFor(int number) {
        return Map.of("Item " + number, List.of("Value " + number));
    }

    private String text() {
        return output.toString(StandardCharsets.UTF_8);
    }
}