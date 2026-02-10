package com.epam.infrastructure.persistence.dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "trainees")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
public class TraineeDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "trainee_id")
    @EqualsAndHashCode.Include
    private Long traineeId;

    @OneToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserDAO userDAO;

    @OneToMany(mappedBy = "traineeDAO", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TrainingDAO> trainingDAOS = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id"))
    private List<TrainerDAO> trainerDAOS = new ArrayList<>();

    @Column(name = "date_of_birth")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @Column(name = "address")
    private String address;

    public TraineeDAO() {}

    public void addTraining(TrainingDAO trainingDAO) {
        if (!trainingDAOS.contains(trainingDAO)) {
            trainingDAOS.add(trainingDAO);
        }
        trainingDAO.setTraineeDAO(this);
    }

    public void addTrainer(TrainerDAO trainerDAO) {
        if (!trainerDAOS.contains(trainerDAO)) {
            trainerDAOS.add(trainerDAO);
        }
    }
}
