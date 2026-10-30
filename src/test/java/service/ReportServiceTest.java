package org.keyin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keyin.client.ApiClient;
import org.mockito.MockMakers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    private ApiClient apiClient;
    private ReportService service;

    private final Map<String, List<String>> cityReport =
            Map.of("Toronto", List.of("YYZ"));

    private final Map<String, List<String>> passengerAircraftReport =
            Map.of("Ahmed Ali", List.of("Airbus A320"));

    private final Map<String, List<String>> aircraftAirportsReport =
            Map.of("Airbus A320", List.of("DXB", "FRA"));

    private final Map<String, List<String>> passengerAirportsReport =
            Map.of("Ahmed Ali", List.of("DXB", "FRA"));

    @BeforeEach
    void setUp() {
        apiClient = mock(
                ApiClient.class,
                withSettings().mockMaker(MockMakers.SUBCLASS)
        );

        service = new ReportService(apiClient);
    }

    private void prepareSuccessfulResponses() throws Exception {
        when(apiClient.getAirportsByCity())
                .thenReturn(cityReport);

        when(apiClient.getAircraftByPassenger())
                .thenReturn(passengerAircraftReport);

        when(apiClient.getAirportsByAircraft())
                .thenReturn(aircraftAirportsReport);

        when(apiClient.getAirportsUsedByPassenger())
                .thenReturn(passengerAirportsReport);
    }

    @Test
    void loadsEachReportIntoTheCorrectQuestion() throws Exception {
        prepareSuccessfulResponses();

        service.loadReports();

        assertEquals(cityReport, service.getReport(1));
        assertEquals(passengerAircraftReport, service.getReport(2));
        assertEquals(aircraftAirportsReport, service.getReport(3));
        assertEquals(passengerAirportsReport, service.getReport(4));

        verifyFourCallsOnly();
    }

    @Test
    void readingSavedReportsDoesNotMakeMoreApiCalls() throws Exception {
        prepareSuccessfulResponses();
        service.loadReports();

        for (int question = 1; question <= 4; question++) {
            service.getReport(question);
            service.getReport(question);
        }

        verifyFourCallsOnly();
    }

    @Test
    void secondLoadIsRejectedWithoutMoreApiCalls() throws Exception {
        prepareSuccessfulResponses();
        service.loadReports();

        assertThrows(
                IllegalStateException.class,
                () -> service.loadReports()
        );

        verifyFourCallsOnly();
    }

    @Test
    void failedLoadDoesNotExposePartialReportsOrRetry() throws Exception {
        when(apiClient.getAirportsByCity())
                .thenReturn(cityReport);

        when(apiClient.getAircraftByPassenger())
                .thenThrow(new IOException("Server unavailable"));

        assertThrows(
                IOException.class,
                () -> service.loadReports()
        );

        // Even the first successful response must remain unavailable.
        assertThrows(
                IllegalStateException.class,
                () -> service.getReport(1)
        );

        // A failed attempt must not trigger an automatic second load.
        assertThrows(
                IllegalStateException.class,
                () -> service.loadReports()
        );

        verify(apiClient).getAirportsByCity();
        verify(apiClient).getAircraftByPassenger();
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    void reportCannotBeReadBeforeLoading() {
        assertThrows(
                IllegalStateException.class,
                () -> service.getReport(1)
        );

        verifyNoInteractions(apiClient);
    }

    @Test
    void rejectsQuestionNumbersOutsideOneToFour() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getReport(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getReport(5)
        );

        verifyNoInteractions(apiClient);
    }

    private void verifyFourCallsOnly() throws Exception {
        verify(apiClient).getAirportsByCity();
        verify(apiClient).getAircraftByPassenger();
        verify(apiClient).getAirportsByAircraft();
        verify(apiClient).getAirportsUsedByPassenger();

        verifyNoMoreInteractions(apiClient);
    }
}