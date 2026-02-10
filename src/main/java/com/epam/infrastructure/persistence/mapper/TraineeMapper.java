package com.epam.infrastructure.persistence.mapper;

import com.epam.domain.model.Trainee;
import com.epam.domain.model.UserRole;
import com.epam.infrastructure.persistence.dao.TraineeDAO;
import com.epam.infrastructure.persistence.dao.UserDAO;
import com.epam.infrastructure.persistence.exception.MappingException;
import org.springframework.lang.NonNull;

public final class TraineeMapper {

    private TraineeMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static TraineeDAO toEntity(@NonNull Trainee trainee) {
        UserDAO userDAO = new UserDAO();
        userDAO.setUserId(trainee.getUserId());
        userDAO.setFirstName(trainee.getFirstName());
        userDAO.setLastName(trainee.getLastName());
        userDAO.setUsername(trainee.getUsername());
        userDAO.setPassword(trainee.getPassword());
        userDAO.setUserRole(UserRole.TRAINEE);
        userDAO.setActive(trainee.getActive());

        TraineeDAO traineeDAO = new TraineeDAO();
        traineeDAO.setUserDAO(userDAO);
        traineeDAO.setTraineeId(trainee.getTraineeId());
        traineeDAO.setDob(trainee.getDob());
        traineeDAO.setAddress(trainee.getAddress());

        userDAO.setTraineeDAO(traineeDAO);

        return traineeDAO;
    }

    public static Trainee toDomain(@NonNull TraineeDAO traineeDAO) {
        UserDAO userDAO = traineeDAO.getUserDAO();
        if (userDAO == null) {
            throw new MappingException("Cannot map TraineeDAO to Trainee: UserDAO is null");
        }

        Trainee trainee = new Trainee(userDAO.getFirstName(), userDAO.getLastName(), userDAO.getActive());

        trainee.setTraineeId(traineeDAO.getTraineeId());
        trainee.setUserId(userDAO.getUserId());
        trainee.setUsername(userDAO.getUsername());
        trainee.setPassword(userDAO.getPassword());
        trainee.setDob(traineeDAO.getDob());
        trainee.setAddress(traineeDAO.getAddress());

        return trainee;
    }

    public static void updateEntity(TraineeDAO dao, Trainee trainee) {
        dao.getUserDAO().setFirstName(trainee.getFirstName());
        dao.getUserDAO().setLastName(trainee.getLastName());
        dao.getUserDAO().setPassword(trainee.getPassword());
        dao.getUserDAO().setActive(trainee.getActive());
        dao.setDob(trainee.getDob());
        dao.setAddress(trainee.getAddress());
    }
}
