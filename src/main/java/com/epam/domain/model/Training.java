package com.epam.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Training {

	private final Trainee trainee;

	private final Trainer trainer;

	@EqualsAndHashCode.Include
	@Setter
	private Long trainingId;

	@Setter
	private String trainingName;

	@Setter
	private LocalDateTime trainingDate;

	@Setter
	private Integer trainingDurationMin;

	@Setter
	private TrainingType trainingType;

	public Training(String trainingName, LocalDateTime trainingDate, Integer trainingDurationMin, Trainee trainee,
			Trainer trainer, TrainingType trainingType) {
		this.trainingName = trainingName;
		this.trainingDate = trainingDate;
		this.trainingDurationMin = trainingDurationMin;
		this.trainee = trainee;
		this.trainer = trainer;
		this.trainingType = trainingType;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Long trainingId;

		private String trainingName;

		private LocalDateTime trainingDate;

		private Integer trainingDurationMin;

		private TrainingType trainingType;

		private Trainee trainee;

		private Trainer trainer;

		public Builder trainingId(Long trainingId) {
			this.trainingId = trainingId;
			return this;
		}

		public Builder trainingName(String trainingName) {
			this.trainingName = trainingName;
			return this;
		}

		public Builder trainingDate(LocalDateTime trainingDate) {
			this.trainingDate = trainingDate;
			return this;
		}

		public Builder trainingDurationMin(Integer trainingDurationMin) {
			this.trainingDurationMin = trainingDurationMin;
			return this;
		}

		public Builder trainingType(TrainingType trainingType) {
			this.trainingType = trainingType;
			return this;
		}

		public Builder trainee(Trainee trainee) {
			this.trainee = trainee;
			return this;
		}

		public Builder trainer(Trainer trainer) {
			this.trainer = trainer;
			return this;
		}

		public Training build() {
			Training training = new Training(trainingName, trainingDate, trainingDurationMin, trainee, trainer,
					trainingType);
			training.setTrainingId(trainingId);
			return training;
		}

	}

}
