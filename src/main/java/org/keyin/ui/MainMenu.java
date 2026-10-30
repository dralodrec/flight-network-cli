package org.keyin.ui;

import org.keyin.history.HistoryManager;
import org.keyin.history.ReportRecord;
import org.keyin.service.ReportService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class MainMenu {

    private final ReportService reportService;
    private final ConsolePrinter printer;
    private final HistoryManager history;
    private final Scanner scanner;

    public MainMenu(
            ReportService reportService,
            ConsolePrinter printer,
            HistoryManager history,
            Scanner scanner) {

        this.reportService = reportService;
        this.printer = printer;
        this.history = history;
        this.scanner = scanner;
    }

    public void start() {

        while (true) {
            printMenu();

            if (!scanner.hasNextLine()) {
                return;
            }

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1", "2", "3", "4" ->
                        showReport(Integer.parseInt(choice));

                case "5" -> showHistory();

                case "6" -> showPreviousRecord();

                case "0" -> {
                    System.out.println("Goodbye!");
                    return;
                }

                default ->
                        System.out.println("Invalid choice. Enter 0 to 6.");
            }
        }
    }

    private void printMenu() {
        System.out.println("""
                
                =================================
                       Flight Network CLI
                =================================
                1. Airports by City
                2. Aircraft by Passenger
                3. Airports by Aircraft
                4. Airports Used by Passenger
                5. View History
                6. Previous Record
                0. Exit
                """);

        System.out.print("Choose an option: ");
    }

    private void showReport(int questionNumber) {
        String title = reportService.getTitle(questionNumber);

        printer.printReport(
                title,
                reportService.getReport(questionNumber)
        );

        history.addRecord(questionNumber, title);
    }

    private void showHistory() {
        List<ReportRecord> records = history.getHistory();

        if (records.isEmpty()) {
            System.out.println("No reports viewed yet.");
            return;
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        System.out.println("\nViewing History");
        System.out.println("--------------------------------");

        for (int i = 0; i < records.size(); i++) {
            ReportRecord record = records.get(i);

            System.out.println(
                    (i + 1) + ". "
                            + record.title() + " | "
                            + record.viewedAt().format(formatter)
            );
        }
    }

    private void showPreviousRecord() {
        ReportRecord previous = history.getPreviousRecord();

        if (previous == null) {
            System.out.println(
                    "View at least two reports to see a previous record."
            );
            return;
        }

        printer.printReport(
                "Previous: " + previous.title(),
                reportService.getReport(previous.questionNumber())
        );
    }
}