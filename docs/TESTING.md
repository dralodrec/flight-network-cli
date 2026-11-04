# Unit Testing Guide — Flight Network CLI

## Purpose

The tests check that the CLI requests reports correctly, caches data, handles menu input, displays results, and records viewing history.

Testing each component separately helps identify where a problem occurs. For example, a formatting problem should be investigated in `ConsolePrinter`, while an incorrect HTTP request should be investigated in `ApiClient`.

## Tools Used

| Tool | Purpose |
|---|---|
| JUnit Jupiter | Defines and runs test cases |
| Mockito | Replaces selected dependencies with controlled test doubles |
| Maven Surefire | Runs the tests through Maven |
| Java HTTP Server | Provides controlled HTTP responses for API client tests |
| GitHub Actions | Runs tests automatically on pull requests and branch updates |

## Running the Tests

From the project root:

```bash
mvn clean test
```

Run one test class:

```bash
mvn -Dtest=HistoryManagerTest test
```

You can also run a test class or individual test method using the run button in IntelliJ IDEA.

The tests do not require:

- MySQL.
- The partner's real API.
- A manually started `MockApiServer`.

The API client tests start and stop their own HTTP server.

## Test Suite Summary

The completed suite contains 35 tests.

| Test class | Number of tests | Component being checked |
|---|---:|---|
| `HistoryManagerTest` | 8 | Viewing history |
| `ReportServiceTest` | 6 | Report loading and caching |
| `ApiClientTest` | 9 | HTTP communication and JSON conversion |
| `MainMenuTest` | 8 | User interaction |
| `ConsolePrinterTest` | 4 | Console formatting |
| **Total** | **35** | |

The behaviour descriptions below summarize the coverage. Some individual tests check several related conditions.

## How a Test Is Structured

Tests generally follow three steps:

1. **Arrange:** Prepare the objects, input, and expected result.
2. **Act:** Call the method being tested.
3. **Assert:** Check that the actual result matches the expected behaviour.

For example, this illustrates testing a new history manager:

```java
@Test
void newHistoryIsEmpty() {
    // Arrange
    HistoryManager history = new HistoryManager();

    // Act
    List<ReportRecord> records = history.getHistory();

    // Assert
    assertTrue(records.isEmpty());
}
```

The example assumes the corresponding project imports.

The assertion fails if a new history manager unexpectedly contains records.

## 1. HistoryManagerTest

### Purpose

Checks how the application records and retrieves reports viewed during a session.

### Behaviour Covered

- A new history starts empty.
- Adding a record preserves its report number, title, and viewing time.
- Records remain in viewing order.
- Viewing the same report repeatedly creates separate history entries.
- A previously returned history snapshot does not change when new records are added.
- Callers cannot modify the returned history list.
- Fewer than two records means no previous report is available.
- With enough records, the previous report is the second-most-recent entry.

### Why These Tests Matter

History should accurately describe the user's actions.

For example, if the user views reports 1, 3, and 2, the previous report is report 3. The tests check that the application uses viewing order rather than report number.

Protecting the returned list prevents other classes from accidentally changing the stored history.

### Dependencies

These tests use the real `HistoryManager`. No HTTP server or Mockito mock is needed.

## 2. ReportServiceTest

### Purpose

Checks that the four reports are loaded once and made available from memory.

### Behaviour Covered

- Loading reports calls all four API client methods.
- Retrieved reports are available from the cache.
- Reading cached reports does not trigger additional API calls.
- A second loading attempt is rejected.
- A loading failure does not expose partially loaded reports.
- A failed loading attempt cannot silently be retried through another load call.
- Access before successful loading is rejected.
- Invalid report numbers are rejected.

### Why These Tests Matter

The CLI must make exactly four GET requests during a successful startup.

The service tests check the application-level rule: each of the four API client methods is called once, and browsing cached data does not call them again.

Preventing partial loading means the menu cannot display a mixture of available and missing reports after a startup failure.

### How Mockito Is Used

The real `ApiClient` is replaced with a mock.

The mock returns prepared report maps or throws an exception when a failure needs to be tested.

For example:

```java
when(apiClient.getAirportsByCity())
        .thenReturn(Map.of("Toronto", List.of("YYZ")));
```

This sets up a controlled response without making an HTTP request.

A verification such as:

```java
verify(apiClient, times(1)).getAirportsByCity();
```

checks how many times the service called that method.

## 3. ApiClientTest

### Purpose

Checks that `ApiClient` sends the expected HTTP requests and correctly handles the responses.

### Behaviour Covered

Four tests cover the four report endpoints. They check:

- The requested path.
- The GET method.
- The `Accept: application/json` header.
- Conversion of the JSON response into the expected Java map.

The remaining five tests cover:

- An empty JSON object.
- An HTTP server error.
- Malformed JSON.
- A JSON `null` response.
- An empty response body.

### Why These Tests Matter

A correct menu is not enough if the client sends a request to the wrong endpoint or cannot understand the response.

These tests also distinguish a valid empty report from a missing or invalid response.

For example:

```json
{}
```

represents an empty report, while an empty response body does not contain a report object.

### How the Test Server Works

Each test uses a local HTTP server with a dynamically assigned port.

The server returns a response chosen by the test and records details of the request it receives.

The server is stopped after the test finishes.

These tests use real HTTP communication between the client and the local test server. They are focused integration tests rather than purely isolated unit tests.

They verify the client's HTTP behaviour, but do not verify the partner's database or backend implementation.

## 4. MainMenuTest

### Purpose

Checks the user's interaction with the console menu.

### Behaviour Covered

- Selecting `0` exits.
- Reaching the end of input exits cleanly.
- Options 1–4 display the appropriate reports and record their history.
- Invalid input is handled without preventing later valid selections.
- Empty history produces an appropriate message.
- Existing history is displayed in viewing order.
- The previous-report option requires at least two report views.
- Repeatedly viewing the previous report does not add more history entries.

### Why These Tests Matter

The program should remain usable when someone enters an invalid number, a word, or a blank line.

The history tests also ensure that looking at history does not change it.

### How Input Is Simulated

A `Scanner` reads prepared text instead of waiting for keyboard input.

For example:

```java
Scanner scanner = new Scanner("1\n5\n0\n");
```

This represents a user who:

1. Selects report 1.
2. Displays viewing history.
3. Exits.

### How Dependencies Are Handled

The menu tests use mocked report and printing dependencies where appropriate, alongside a real history manager.

This allows the tests to check whether the menu requests the correct actions without needing HTTP communication.

Console output is captured when a test needs to check messages. The original output stream is restored afterward.

## 5. ConsolePrinterTest

### Purpose

Checks how report data is displayed to the user.

### Behaviour Covered

- Report titles and data rows are printed.
- An empty report displays `No records found.`
- An empty value list displays `None listed`.
- A null value list also displays `None listed`.

### Why These Tests Matter

Reports should remain readable even when the API returns no records or a label has no associated values.

For example, a normal row might be:

```text
Toronto: YYZ
```

A row without values should display a useful message instead of failing.

### How Output Is Checked

The tests temporarily redirect `System.out` to an in-memory output stream.

They then inspect the captured text and restore the original output stream after the test.

The tests check the relevant content without depending on an unspecified map iteration order.

## Understanding Common Assertions

| Assertion or verification | What it checks |
|---|---|
| `assertEquals(expected, actual)` | Two values match |
| `assertTrue(condition)` | A condition is true |
| `assertNull(value)` | No value was returned |
| `assertThrows(...)` | An operation throws the expected exception |
| `verify(mock)` | An expected call was made |
| `times(1)` | A call occurred exactly once |

Assertions check results. Mockito verifications check interactions between objects.

Both are useful: a method could return the correct result while making unnecessary API calls.

## Test Isolation

Each test should prepare the state it needs and clean up resources it changes.

The suite uses techniques such as:

- Creating fresh objects before tests.
- Preparing controlled mock responses.
- Starting HTTP servers on dynamically assigned ports.
- Stopping test servers afterward.
- Restoring `System.out` after capturing output.

This reduces interference between tests and makes failures easier to reproduce.

## Reading Test Results

A successful run of the current suite should report:

```text
Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Result | Meaning |
|---|---|
| Tests run | Number of executed tests |
| Failures | Assertions that did not match expectations |
| Errors | Unexpected exceptions or setup problems |
| Skipped | Tests that were not executed |

Detailed reports are available in:

```text
target/surefire-reports/
```

When a test fails, start with its name, the failure message, and the referenced source line.

## GitHub Actions

The workflow is stored in:

```text
.github/workflows/tests.yml
```

It runs tests on pull requests targeting `main` or `develop`, and on pushes to those branches.

Its test command is:

```bash
mvn --batch-mode --no-transfer-progress clean test -DfailIfNoTests=true
```

The `failIfNoTests` option prevents a build from passing when no tests are discovered.

Automated testing helps catch regressions before changes are merged.

## What the Tests Do Not Prove

The passing suite does not independently establish:

- Compatibility with the partner's running API.
- Correctness of backend database relationships.
- Accuracy of real flight data.
- Completion of backend pagination and sorting.
- A particular code coverage percentage.

Test count and code coverage are different measurements. This project has 35 tests, but no coverage percentage is claimed here.

Final integration verification should run the CLI against the real API and check all four reports.

## Testing Summary

The tests cover the CLI's main responsibilities: requesting reports, handling responses, caching data, processing menu input, displaying reports, and tracking viewing history.

Mocks keep service and menu tests focused. A local HTTP server allows the API client to be tested with controlled responses. GitHub Actions repeats these checks automatically as the project changes.