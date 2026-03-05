package com.epam.integration;

import java.util.List;

import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.infrastructure.persistence.dao.TrainingTypeDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class GymIntegrationTestBase extends IntegrationTestBase {

    @PersistenceContext
    protected EntityManager entityManager;

    @BeforeEach
    void cleanDatabase() {
        // Order matters — FK constraints must be respected
        entityManager.createQuery("DELETE FROM TrainingDAO").executeUpdate();
        entityManager.createQuery("DELETE FROM TraineeDAO").executeUpdate();
        entityManager.createQuery("DELETE FROM TrainerDAO").executeUpdate();
        entityManager.createQuery("DELETE FROM UserDAO").executeUpdate();
        entityManager.createQuery("DELETE FROM TrainingTypeDAO").executeUpdate();
        entityManager.flush();
    }

    /**
     * Returns an existing TrainingType for the given enum value, creating it if it
     * doesn't already exist in the current transaction. Safe to call multiple times
     * with the same value within one test.
     */
    protected TrainingType requireTrainingType(TrainingTypeEnum type) {
        List<TrainingTypeDAO> results = entityManager
                .createQuery("SELECT t FROM TrainingTypeDAO t WHERE t.trainingTypeName = :name", TrainingTypeDAO.class)
                .setParameter("name", type)
                .getResultList();

        if (!results.isEmpty()) {
            TrainingTypeDAO dao = results.get(0);
            TrainingType domain = new TrainingType(dao.getTrainingTypeName());
            domain.setTrainingTypeId(dao.getTrainingTypeId());
            return domain;
        }

        TrainingTypeDAO dao = new TrainingTypeDAO();
        dao.setTrainingTypeName(type);
        entityManager.persist(dao);
        entityManager.flush();

        TrainingType domain = new TrainingType(type);
        domain.setTrainingTypeId(dao.getTrainingTypeId());
        return domain;
    }
}
