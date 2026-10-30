package org.keyin;

import org.keyin.client.ApiClient;
import org.keyin.history.HistoryManager;
import org.keyin.service.ReportService;
import org.keyin.ui.ConsolePrinter;
import org.keyin.ui.MainMenu;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        String baseUrl = "http://localhost:8080";

        if (args.length > 0) {
            baseUrl = args[0];
        }

        ApiClient apiClient = new ApiClient(baseUrl);
        ReportService reportService = new ReportService(apiClient);

        try {
            System.out.println("Loading reports...");
            reportService.loadReports();

            ConsolePrinter printer = new ConsolePrinter();
            HistoryManager history = new HistoryManager();

            try (Scanner scanner = new Scanner(System.in)) {
                MainMenu menu = new MainMenu(
                        reportService,
                        printer,
                        history,
                        scanner
                );

                menu.start();
            }

        } catch (IOException e) {
            System.err.println(
                    "Could not load reports: " + e.getMessage()
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Report loading was interrupted.");
        }
    }
}