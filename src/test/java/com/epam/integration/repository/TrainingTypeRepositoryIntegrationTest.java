package com.epam.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import com.epam.application.exception.EntityNotFoundException;
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.domain.port.TrainingTypeRepository;
import com.epam.integration.base.SeededIntegrationTestBase;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TrainingTypeRepositoryIntegrationTest extends SeededIntegrationTestBase {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void getTrainingTypes_returnsAllSixSeededTypes() {
        List<TrainingType> result = trainingTypeRepository.getTrainingTypes();

        assertThat(result).hasSize(6);
        assertThat(result)
                .extracting(t -> t.getTrainingTypeName().name())
                .containsExactlyInAnyOrder("CARDIO", "STRENGTH", "YOGA", "CROSSFIT", "PILATES", "BOXING");
    }

    @Test
    void findByTrainingTypeName_withExistingType_returnsType() {
        Optional<TrainingType> result = trainingTypeRepository.findByTrainingTypeName(TrainingTypeEnum.YOGA);

        assertThat(result).isPresent();
        assertThat(result.get().getTrainingTypeName()).isEqualTo(TrainingTypeEnum.YOGA);
        assertThat(result.get().getTrainingTypeId()).isNotNull();
    }

    @Test
    void findByTrainingTypeName_coversAllSixEnumValues() {
        for (TrainingTypeEnum type : TrainingTypeEnum.values()) {
            Optional<TrainingType> result = trainingTypeRepository.findByTrainingTypeName(type);
            assertThat(result)
                    .as("Expected to find training type %s in seed data", type)
                    .isPresent();
        }
    }

    @Test
    void findByTrainingTypeName_withNonExistentType_returnsEmpty() {
        entityManager
                .createQuery("DELETE FROM TrainingTypeDAO t WHERE t.trainingTypeName = :name")
                .setParameter("name", TrainingTypeEnum.BOXING)
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        Optional<TrainingType> result = trainingTypeRepository.findByTrainingTypeName(TrainingTypeEnum.BOXING);

        assertThat(result).isEmpty();
    }

    @Test
    void findById_withExistingId_returnsType() {
        Long id = resolveId(TrainingTypeEnum.CARDIO);

        Optional<TrainingType> result = trainingTypeRepository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getTrainingTypeName()).isEqualTo(TrainingTypeEnum.CARDIO);
    }

    @Test
    void findById_withNonExistentId_returnsEmpty() {
        Optional<TrainingType> result = trainingTypeRepository.findById(Long.MAX_VALUE);

        assertThat(result).isEmpty();
    }

    @Test
    void save_newType_persistsAndReturnsWithId() {
        // Delete BOXING first so we can re-insert a clean one
        entityManager
                .createQuery("DELETE FROM TrainingTypeDAO t WHERE t.trainingTypeName = :name")
                .setParameter("name", TrainingTypeEnum.BOXING)
                .executeUpdate();
        entityManager.flush();

        TrainingType newType = new TrainingType(TrainingTypeEnum.BOXING);

        TrainingType saved = trainingTypeRepository.save(newType);

        assertThat(saved.getTrainingTypeId()).isNotNull();
        assertThat(saved.getTrainingTypeName()).isEqualTo(TrainingTypeEnum.BOXING);

        assertThat(trainingTypeRepository.findByTrainingTypeName(TrainingTypeEnum.BOXING))
                .isPresent();
    }

    @Test
    void save_existingType_updatesRecord() {
        Long id = resolveId(TrainingTypeEnum.CARDIO);

        // Re-save the same type (merge path)
        TrainingType existing = new TrainingType(TrainingTypeEnum.CARDIO);
        existing.setTrainingTypeId(id);

        TrainingType saved = trainingTypeRepository.save(existing);

        assertThat(saved.getTrainingTypeId()).isEqualTo(id);
        assertThat(saved.getTrainingTypeName()).isEqualTo(TrainingTypeEnum.CARDIO);
    }

    @Test
    void delete_withExistingId_removesRecord() {
        Long id = resolveId(TrainingTypeEnum.PILATES);

        trainingTypeRepository.delete(id);
        entityManager.flush();
        entityManager.clear();

        assertThat(trainingTypeRepository.findById(id)).isEmpty();
    }

    @Test
    void delete_withNonExistentId_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> trainingTypeRepository.delete(Long.MAX_VALUE))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private Long resolveId(TrainingTypeEnum type) {
        return entityManager
                .createQuery(
                        "SELECT t.trainingTypeId FROM TrainingTypeDAO t WHERE t.trainingTypeName = :name", Long.class)
                .setParameter("name", type)
                .getSingleResult();
    }
}
