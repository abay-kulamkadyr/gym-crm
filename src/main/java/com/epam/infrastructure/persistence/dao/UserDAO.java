package com.epam.infrastructure.persistence.dao;

import com.epam.domain.model.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "users", indexes = @Index(name = "idx_username", columnList = "username"))
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "user_id")
    @EqualsAndHashCode.Include
    private Long userId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @OneToOne(mappedBy = "userDAO", fetch = FetchType.LAZY)
    private TraineeDAO traineeDAO;

    @OneToOne(mappedBy = "userDAO", fetch = FetchType.LAZY)
    private TrainerDAO trainerDAO;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    public UserDAO() {}
}
