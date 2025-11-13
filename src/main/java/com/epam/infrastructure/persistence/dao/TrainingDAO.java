package com.epam.infrastructure.persistence.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "trainings")
@Getter
@Setter
public class TrainingDAO {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "training_id")
	private Long trainingId;

	@ManyToOne
	@JoinColumn(name = "trainee_id", nullable = false)
	private TraineeDAO traineeDAO;

	@ManyToOne
	@JoinColumn(name = "trainer_id", nullable = false)
	private TrainerDAO trainerDAO;

	@ManyToOne
	@JoinColumn(name = "training_type_id", nullable = false)
	private TrainingTypeDAO trainingTypeDAO;

	@Column(name = "training_name", nullable = false)
	private String trainingName;

	@Column(name = "training_date", nullable = false)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime trainingDate;

	@Column(name = "training_duration", nullable = false)
	private Integer trainingDurationMin;

	public TrainingDAO() {

	}

}
