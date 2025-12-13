package com.epam.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.domain.port.TrainingTypeRepository;
import com.epam.infrastructure.persistence.dao.TrainingTypeDAO;
import com.epam.infrastructure.persistence.mapper.TrainingTypeMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class TrainingTypeRepositoryImpl implements TrainingTypeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public TrainingType save(@NonNull TrainingType trainingType) {
        TrainingTypeDAO entity = TrainingTypeMapper.toEntity(trainingType);

        if (trainingType.getTrainingTypeId() == null) {
            entityManager.persist(entity);
        }
        else {
            entityManager.merge(entity);
        }

        return TrainingTypeMapper.toDomain(entity);
    }

    @Override
    public Optional<TrainingType> findById(@NonNull Long id) {
        TrainingTypeDAO trainingTypeDAO = entityManager.find(TrainingTypeDAO.class, id);

        if (trainingTypeDAO == null) {
            log.warn("Training type with ID {} not found", id);
            return Optional.empty();
        }

        return Optional.of(TrainingTypeMapper.toDomain(trainingTypeDAO));
    }

    @Override
    public void delete(@NonNull Long id) {
        TrainingTypeDAO trainingTypeDAO = entityManager.find(TrainingTypeDAO.class, id);

        if (trainingTypeDAO == null) {
            throw new EntityNotFoundException(String.format("Training type with ID %d not found", id));
        }

        entityManager.remove(trainingTypeDAO);
    }

    @Override
    public Optional<TrainingType> findByTrainingTypeName(TrainingTypeEnum trainingTypeName) {
        String jpql = "SELECT t FROM TrainingTypeDAO t WHERE t.trainingTypeName = :trainingTypeName";
        List<TrainingTypeDAO> results = entityManager
                .createQuery(jpql, TrainingTypeDAO.class)
                .setParameter("trainingTypeName", trainingTypeName)
                .getResultList();

        if (results.isEmpty()) {
            log.warn("TrainingType with username '{}' not found", trainingTypeName);
            return Optional.empty();
        }

        return Optional.of(TrainingTypeMapper.toDomain(results.get(0)));
    }

    @Override
    public List<TrainingType> getTrainingTypes() {
        String jpql = "SELECT t FROM TrainingTypeDAO t";
        List<TrainingTypeDAO> results = entityManager.createQuery(jpql, TrainingTypeDAO.class).getResultList();
        return results.stream().map(TrainingTypeMapper::toDomain).toList();
    }

}
