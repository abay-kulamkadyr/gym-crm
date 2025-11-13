package com.epam.infrastructure.persistence.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "training_types")
@Getter
@Setter
public class TrainingTypeDAO {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "training_type_id")
	private Long trainingTypeId;

	@Column(name = "training_type_name", unique = true, nullable = false)
	private String trainingTypeName;

	@OneToMany(mappedBy = "trainingTypeDAO", fetch = FetchType.LAZY)
	private List<TrainerDAO> trainerDAOS = new ArrayList<>();

	@OneToMany(mappedBy = "trainingTypeDAO", fetch = FetchType.LAZY)
	private List<TrainingDAO> trainingDAOS = new ArrayList<>();

	public TrainingTypeDAO() {

	}

	public void addTraining(TrainingDAO trainingDAO) {
		if (!trainingDAOS.contains(trainingDAO)) {
			trainingDAOS.add(trainingDAO);
		}
		trainingDAO.setTrainingTypeDAO(this);
	}

}
