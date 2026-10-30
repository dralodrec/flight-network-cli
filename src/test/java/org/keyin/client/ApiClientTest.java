package org.keyin.client;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ApiClientTest {

    private HttpServer server;
    private ApiClient apiClient;

    private final BlockingQueue<ReceivedRequest> requests =
            new LinkedBlockingQueue<>();

    private record ReceivedRequest(
            String path,
            String method,
            String accept
    ) {}

    @BeforeEach
    void setUp() throws IOException {
        // Port 0 lets the operating system choose an available port.
        server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0
        );

        String baseUrl =
                "http://127.0.0.1:" + server.getAddress().getPort();

        apiClient = new ApiClient(baseUrl);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void getsAirportsByCity() throws Exception {
        respondWith(200, """
                {"Toronto":["YYZ","YTZ"]}
                """);

        Map<String, List<String>> report =
                apiClient.getAirportsByCity();

        assertEquals(
                Map.of("Toronto", List.of("YYZ", "YTZ")),
                report
        );
        assertRequest("/api/reports/airports-by-city");
    }

    @Test
    void getsAircraftByPassenger() throws Exception {
        respondWith(200, """
                {"Ahmed Ali":["A320","A321"]}
                """);

        Map<String, List<String>> report =
                apiClient.getAircraftByPassenger();

        assertEquals(
                Map.of("Ahmed Ali", List.of("A320", "A321")),
                report
        );
        assertRequest("/api/reports/aircraft-by-passenger");
    }

    @Test
    void getsAirportsByAircraft() throws Exception {
        respondWith(200, """
                {"A320":["DXB","FRA"]}
                """);

        Map<String, List<String>> report =
                apiClient.getAirportsByAircraft();

        assertEquals(
                Map.of("A320", List.of("DXB", "FRA")),
                report
        );
        assertRequest("/api/reports/airports-by-aircraft");
    }

    @Test
    void getsAirportsUsedByPassenger() throws Exception {
        respondWith(200, """
                {"Sara Khan":["FRA","YYZ"]}
                """);

        Map<String, List<String>> report =
                apiClient.getAirportsUsedByPassenger();

        assertEquals(
                Map.of("Sara Khan", List.of("FRA", "YYZ")),
                report
        );
        assertRequest("/api/reports/airports-used-by-passenger");
    }

    @Test
    void acceptsAnEmptyReport() throws Exception {
        respondWith(200, "{}");

        assertTrue(apiClient.getAirportsByCity().isEmpty());
    }

    @Test
    void rejectsHttpServerError() {
        respondWith(500, """
                {"message":"Server error"}
                """);

        IOException error = assertThrows(
                IOException.class,
                () -> apiClient.getAirportsByCity()
        );

        assertTrue(error.getMessage().contains("500"));
    }

    @Test
    void rejectsMalformedJson() {
        respondWith(200, "{\"Toronto\":");

        IOException error = assertThrows(
                IOException.class,
                () -> apiClient.getAirportsByCity()
        );

        assertEquals(
                "The API returned an invalid report.",
                error.getMessage()
        );
        assertNotNull(error.getCause());
    }

    @Test
    void rejectsJsonNull() {
        respondWith(200, "null");

        IOException error = assertThrows(
                IOException.class,
                () -> apiClient.getAirportsByCity()
        );

        assertEquals(
                "The API returned an empty or null response.",
                error.getMessage()
        );
    }

    @Test
    void rejectsAnEmptyResponseBody() {
        respondWith(200, "");

        IOException error = assertThrows(
                IOException.class,
                () -> apiClient.getAirportsByCity()
        );

        assertEquals(
                "The API returned an empty or null response.",
                error.getMessage()
        );
    }

    private void respondWith(int status, String body) {
        server.createContext("/", exchange -> {
            try {
                requests.add(new ReceivedRequest(
                        exchange.getRequestURI().getPath(),
                        exchange.getRequestMethod(),
                        exchange.getRequestHeaders().getFirst("Accept")
                ));

                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json; charset=UTF-8"
                );

                if (bytes.length == 0) {
                    exchange.sendResponseHeaders(status, -1);
                } else {
                    exchange.sendResponseHeaders(status, bytes.length);
                    exchange.getResponseBody().write(bytes);
                }
            } finally {
                exchange.close();
            }
        });

        server.start();
    }

    private void assertRequest(String expectedPath)
            throws InterruptedException {

        ReceivedRequest request = requests.poll(2, TimeUnit.SECONDS);

        assertNotNull(request, "The server should receive a request");
        assertEquals(expectedPath, request.path());
        assertEquals("GET", request.method());
        assertEquals("application/json", request.accept());
        assertTrue(requests.isEmpty(), "No extra requests expected");
    }
}