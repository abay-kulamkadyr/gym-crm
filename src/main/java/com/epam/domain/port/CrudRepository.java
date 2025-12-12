package com.epam.domain.port;

import java.util.Optional;

import org.springframework.lang.NonNull;

public interface CrudRepository<T> {

	T save(@NonNull T entity);

	Optional<T> findById(@NonNull Long id);

	void delete(@NonNull Long id);

}
