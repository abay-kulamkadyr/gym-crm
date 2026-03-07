Feature: Trainer workload integration

  Scenario: Create trainings for two trainees, delete one, verify trainer workload
    Given the following users exist in the system:
      | role    | firstName | lastName | specialization |
      | TRAINER | John      | Smith    | CARDIO         |
      | TRAINEE | David     | Davis    |                |
      | TRAINEE | Lisa      | Miller   |                |
    When I add the following trainings:
      | trainer    | trainee     | name           | date                | minutes |
      | John.Smith | David.Davis | Morning Cardio | 2026-07-01T09:00:00 | 60      |
      | John.Smith | David.Davis | Evening Cardio | 2026-07-02T18:00:00 | 90      |
      | John.Smith | Lisa.Miller | Cardio Blast   | 2026-07-03T10:00:00 | 120     |
    When I delete the trainee "David.Davis"
    Then the total workload minutes for trainer "John.Smith" for month "JULY" year 2026 should be 120
