package org.keyin.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public Map<String, List<String>> getReport(String endpoint)
            throws IOException, InterruptedException {

        URI uri = URI.create(baseUrl + endpoint);


        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "API request failed. HTTP status: "
                            + response.statusCode()
            );
        }

        Type reportType =
                new TypeToken<Map<String, List<String>>>() {}.getType();

        try {
            Map<String, List<String>> report =
                    gson.fromJson(response.body(), reportType);

            if (report == null) {
                throw new IOException("The API returned an empty or null response.");
            }

            return report;

        } catch (JsonParseException e) {
            throw new IOException("The API returned an invalid report.", e);
        }
    }

    public Map<String, List<String>> getAirportsByCity()
            throws IOException, InterruptedException {

        return getReport("/api/reports/airports-by-city");
    }

    public Map<String, List<String>> getAircraftByPassenger()
            throws IOException, InterruptedException {

        return getReport("/api/reports/aircraft-by-passenger");
    }

    public Map<String, List<String>> getAirportsByAircraft()
            throws IOException, InterruptedException {

        return getReport("/api/reports/airports-by-aircraft");
    }

    public Map<String, List<String>> getAirportsUsedByPassenger()
            throws IOException, InterruptedException {

        return getReport("/api/reports/airports-used-by-passenger");
    }
}