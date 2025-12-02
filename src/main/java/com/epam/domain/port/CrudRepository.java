package com.epam.domain.port;

import org.springframework.lang.NonNull;

import java.util.Optional;

public interface CrudRepository<T> {

	T save(@NonNull T entity);

	Optional<T> findById(@NonNull Long id);

	void delete(@NonNull Long id);

}