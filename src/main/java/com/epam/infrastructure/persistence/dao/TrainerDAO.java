package com.epam.infrastructure.persistence.dao;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "trainers")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class TrainerDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "trainer_id")
    @EqualsAndHashCode.Include
    private Long trainerId;

    @OneToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserDAO userDAO;

    @ManyToOne
    @JoinColumn(name = "training_type_id")
    private TrainingTypeDAO trainingTypeDAO;

    @OneToMany(mappedBy = "trainerDAO", fetch = FetchType.EAGER)
    private List<TrainingDAO> trainingDAOS = new ArrayList<>();

    @ManyToMany(mappedBy = "trainerDAOS", fetch = FetchType.LAZY)
    private List<TraineeDAO> traineeDAOS = new ArrayList<>();

    public TrainerDAO() {

    }

    public void addTraining(TrainingDAO trainingDAO) {
        if (!trainingDAOS.contains(trainingDAO)) {
            trainingDAOS.add(trainingDAO);
        }
        trainingDAO.setTrainerDAO(this);
    }

    public void addTrainee(TraineeDAO traineeDAO) {
        if (!traineeDAOS.contains(traineeDAO)) {
            traineeDAOS.add(traineeDAO);
        }
    }

}
