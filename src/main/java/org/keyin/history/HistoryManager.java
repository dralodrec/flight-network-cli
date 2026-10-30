package org.keyin.history;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    private final List<ReportRecord> history = new ArrayList<>();

    public void addRecord(int questionNumber, String title) {
        history.add(
                new ReportRecord(
                        questionNumber,
                        title,
                        LocalDateTime.now()
                )
        );
    }

    public List<ReportRecord> getHistory() {
        return List.copyOf(history);
    }

    public ReportRecord getPreviousRecord() {

        if (history.size() < 2) {
            return null;
        }

        return history.get(history.size() - 2);
    }
}