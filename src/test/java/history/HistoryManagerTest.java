package org.keyin.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    private HistoryManager history;

    @BeforeEach
    void setUp() {
        history = new HistoryManager();
    }

    @Test
    void newHistoryIsEmpty() {
        assertTrue(history.getHistory().isEmpty());
    }

    @Test
    void addingRecordStoresItsDetails() {
        history.addRecord(1, "Airports by City");

        List<ReportRecord> records = history.getHistory();

        assertEquals(1, records.size());

        ReportRecord record = records.get(0);

        assertEquals(1, record.questionNumber());
        assertEquals("Airports by City", record.title());
        assertNotNull(record.viewedAt());
    }

    @Test
    void historyPreservesViewingOrderAndRepeatedSelections() {
        history.addRecord(1, "Airports by City");
        history.addRecord(3, "Airports by Aircraft");
        history.addRecord(1, "Airports by City");

        List<ReportRecord> records = history.getHistory();

        assertEquals(3, records.size());
        assertEquals(1, records.get(0).questionNumber());
        assertEquals(3, records.get(1).questionNumber());
        assertEquals(1, records.get(2).questionNumber());
    }

    @Test
    void previousRecordIsNullWhenHistoryIsEmpty() {
        assertNull(history.getPreviousRecord());
    }

    @Test
    void previousRecordIsNullAfterOnlyOneSelection() {
        history.addRecord(1, "Airports by City");

        assertNull(history.getPreviousRecord());
    }

    @Test
    void previousRecordReturnsSecondMostRecentWithoutChangingHistory() {
        history.addRecord(1, "Airports by City");
        history.addRecord(3, "Airports by Aircraft");
        history.addRecord(2, "Aircraft by Passenger");

        ReportRecord previous = history.getPreviousRecord();

        assertNotNull(previous);
        assertEquals(3, previous.questionNumber());

        // Repeated access should not navigate farther backward.
        assertEquals(previous, history.getPreviousRecord());
        assertEquals(3, history.getHistory().size());
    }

    @Test
    void returnedHistoryCannotBeModified() {
        history.addRecord(1, "Airports by City");

        List<ReportRecord> records = history.getHistory();

        assertThrows(
                UnsupportedOperationException.class,
                () -> records.clear()
        );

        assertEquals(1, history.getHistory().size());
    }

    @Test
    void returnedHistoryIsASnapshot() {
        history.addRecord(1, "Airports by City");

        List<ReportRecord> snapshot = history.getHistory();

        history.addRecord(2, "Aircraft by Passenger");

        assertEquals(1, snapshot.size());
        assertEquals(2, history.getHistory().size());
    }
}