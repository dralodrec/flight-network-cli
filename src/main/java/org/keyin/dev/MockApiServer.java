package org.keyin.dev;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class MockApiServer {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress("localhost", 8080), 0
        );

        addReport(server, "/api/reports/airports-by-city", """
                {
                  "Toronto": ["YYZ"],
                  "Dubai": ["DXB"],
                  "Frankfurt": ["FRA"]
                }
                """);

        addReport(server, "/api/reports/aircraft-by-passenger", """
                {
                  "Ahmed Ali": ["Airbus A320", "Airbus A321"],
                  "Sara Khan": ["Airbus A321"],
                  "Omar Hassan": ["Boeing 737", "Airbus A320"]
                }
                """);

        addReport(server, "/api/reports/airports-by-aircraft", """
                {
                  "Boeing 737": ["YYZ", "DXB"],
                  "Airbus A320": ["DXB", "FRA", "YYZ"],
                  "Airbus A321": ["FRA", "YYZ"]
                }
                """);

        addReport(server, "/api/reports/airports-used-by-passenger", """
                {
                  "Ahmed Ali": ["DXB", "FRA", "YYZ"],
                  "Sara Khan": ["FRA", "YYZ"],
                  "Omar Hassan": ["YYZ", "DXB"]
                }
                """);

        server.start();

        System.out.println("Mock API: http://localhost:8080");
        System.out.println("Four sample report endpoints are ready.");
    }

    private static void addReport(
            HttpServer server,
            String path,
            String json) {

        server.createContext(path, exchange -> {

            // HttpServer contexts match prefixes; require the exact path.
            if (!exchange.getRequestURI().getPath().equals(path)) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }

            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.getResponseHeaders().set("Allow", "GET");
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }

            System.out.println("GET " + path);

            byte[] response = json.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            exchange.sendResponseHeaders(200, response.length);

            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
    }
}