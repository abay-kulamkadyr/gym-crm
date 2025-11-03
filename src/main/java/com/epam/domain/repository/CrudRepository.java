package com.epam.domain.repository;

import java.util.Optional;
import org.springframework.lang.NonNull;

public interface CrudRepository<T> {

	void save(@NonNull T t);

	Optional<T> findById(Long id);

	void delete(Long id);

}
