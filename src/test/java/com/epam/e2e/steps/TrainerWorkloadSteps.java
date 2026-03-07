package com.epam.e2e.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.epam.domain.model.TrainingTypeEnum;
import com.epam.infrastructure.security.core.FeignTokenHolder;
import com.epam.interfaces.web.client.TrainerWorkloadClient;
import com.epam.interfaces.web.client.response.MonthSummaryDTO;
import com.epam.interfaces.web.client.response.TrainerSummaryResponse;
import com.epam.interfaces.web.client.response.YearSummaryDTO;
import com.epam.interfaces.web.dto.request.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;

public class TrainerWorkloadSteps {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private TrainerWorkloadClient workloadClient;

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<String, String> tokenCache = new HashMap<>();

    @Before
    public void clearTokenCache() {
        tokenCache.clear();
        FeignTokenHolder.clear();
    }

    @Given("the following users exist in the system:")
    public void theFollowingUsersExistInTheSystem(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            String role = row.get("role");
            if ("TRAINER".equals(role)) {
                TrainerRegistrationRequest request = new TrainerRegistrationRequest(
                        row.get("firstName"), row.get("lastName"), TrainingTypeEnum.valueOf(row.get("specialization")));

                ResponseEntity<Map> response =
                        testRestTemplate.postForEntity(baseUrl() + "/api/trainers", request, Map.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

                String username = (String) response.getBody().get("username");
                String password = (String) response.getBody().get("password");
                loginAndGetToken(username, password);

            } else {
                TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                        row.get("firstName"), row.get("lastName"), Optional.empty(), Optional.empty());

                ResponseEntity<Map> response =
                        testRestTemplate.postForEntity(baseUrl() + "/api/trainees", request, Map.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

                String username = (String) response.getBody().get("username");
                String password = (String) response.getBody().get("password");
                loginAndGetToken(username, password);
            }
        }
    }

    @When("I add the following trainings:")
    public void iAddTheFollowingTrainings(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            String traineeUsername = row.get("trainee");
            String trainerUsername = row.get("trainer");

            String traineeToken = tokenCache.get(traineeUsername);
            assertThat(traineeToken)
                    .as("No token found for trainee %s", traineeUsername)
                    .isNotNull();

            UpdateTraineeTrainersRequest assignRequest = new UpdateTraineeTrainersRequest(List.of(trainerUsername));
            HttpEntity<UpdateTraineeTrainersRequest> assignEntity =
                    new HttpEntity<>(assignRequest, authHeaders(traineeToken));

            testRestTemplate.exchange(
                    baseUrl() + "/api/trainees/" + traineeUsername + "/trainers",
                    HttpMethod.PUT,
                    assignEntity,
                    Void.class);

            AddTrainingRequest trainingRequest = new AddTrainingRequest(
                    traineeUsername,
                    trainerUsername,
                    row.get("name"),
                    LocalDateTime.parse(row.get("date")),
                    Integer.parseInt(row.get("minutes")));

            HttpEntity<AddTrainingRequest> trainingEntity =
                    new HttpEntity<>(trainingRequest, authHeaders(traineeToken));

            ResponseEntity<Void> response = testRestTemplate.exchange(
                    baseUrl() + "/api/trainings", HttpMethod.POST, trainingEntity, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @When("I delete the trainee {string}")
    public void iDeleteTheTrainee(String username) {
        String token = tokenCache.get(username);
        assertThat(token).as("No token found for %s", username).isNotNull();

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders(token));

        ResponseEntity<Void> response = testRestTemplate.exchange(
                baseUrl() + "/api/trainees/" + username, HttpMethod.DELETE, entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Then("the total workload minutes for trainer {string} for month {string} year {int} should be {int}")
    public void theTotalWorkloadMinutes(String username, String month, int year, int expectedMinutes) {
        FeignTokenHolder.set(tokenCache.get(username));

        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    TrainerSummaryResponse summary = workloadClient.getTrainerSummary(username);

                    long actual = summary.years().stream()
                            .filter(y -> y.year() == year)
                            .map(YearSummaryDTO::months)
                            .flatMap(List::stream)
                            .filter(m -> m.month().equalsIgnoreCase(month))
                            .mapToLong(MonthSummaryDTO::trainingSummaryDuration)
                            .sum();

                    assertThat(actual)
                            .as(
                                    "Expected %d minutes for %s %s/%d but got %d",
                                    expectedMinutes, username, month, year, actual)
                            .isEqualTo(expectedMinutes);
                });
    }

    // Read the port from the environment at runtime
    private String baseUrl() {
        String port = applicationContext.getEnvironment().getProperty("local.server.port");
        return "http://localhost:" + port;
    }

    private String loginAndGetToken(String username, String password) {
        return tokenCache.computeIfAbsent(username, u -> {
            LoginRequest loginRequest = new LoginRequest(u, password);
            ResponseEntity<Map> response =
                    testRestTemplate.postForEntity(baseUrl() + "/api/auth/login", loginRequest, Map.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            return "Bearer " + response.getBody().get("token");
        });
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
