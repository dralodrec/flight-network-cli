package org.keyin.ui;

import java.util.List;
import java.util.Map;

public class ConsolePrinter {

    public void printReport(
            String title,
            Map<String, List<String>> report) {

        System.out.println();
        System.out.println(title);
        System.out.println("--------------------------------");

        if (report.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        for (Map.Entry<String, List<String>> entry
                : report.entrySet()) {

            String label = entry.getKey();
            List<String> values = entry.getValue();

            String details;

            if (values == null || values.isEmpty()) {
                details = "None listed";
            } else {
                details = String.join(", ", values);
            }

            System.out.println(label + ": " + details);
        }
    }
}