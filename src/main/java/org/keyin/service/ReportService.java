package org.keyin.service;

import org.keyin.client.ApiClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    private final ApiClient apiClient;

    private final Map<Integer, Map<String, List<String>>> reports =
            new HashMap<>();

    private boolean loadAttempted;

    public ReportService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void loadReports() throws IOException, InterruptedException {

        if (loadAttempted) {
            throw new IllegalStateException(
                    "Reports were already requested. Restart to load again."
            );
        }

        loadAttempted = true;

        // Collect everything before making the reports available.
        Map<Integer, Map<String, List<String>>> loaded = new HashMap<>();

        loaded.put(1, apiClient.getAirportsByCity());
        loaded.put(2, apiClient.getAircraftByPassenger());
        loaded.put(3, apiClient.getAirportsByAircraft());
        loaded.put(4, apiClient.getAirportsUsedByPassenger());

        reports.putAll(loaded);
    }

    public Map<String, List<String>> getReport(int questionNumber) {

        if (questionNumber < 1 || questionNumber > 4) {
            throw new IllegalArgumentException("Choose a report from 1 to 4.");
        }

        if (reports.isEmpty()) {
            throw new IllegalStateException("Reports have not been loaded.");
        }

        return reports.get(questionNumber);
    }

    public String getTitle(int questionNumber) {
        return switch (questionNumber) {
            case 1 -> "Airports by City";
            case 2 -> "Aircraft by Passenger";
            case 3 -> "Airports by Aircraft";
            case 4 -> "Airports Used by Passenger";
            default -> throw new IllegalArgumentException(
                    "Choose a report from 1 to 4."
            );
        };
    }
}