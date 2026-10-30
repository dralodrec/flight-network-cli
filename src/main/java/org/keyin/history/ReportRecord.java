package org.keyin.history;

import java.time.LocalDateTime;

public record ReportRecord(
        int questionNumber,
        String title,
        LocalDateTime viewedAt
) {
}