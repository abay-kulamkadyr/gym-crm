package com.epam.domain.repository;

import java.util.Collection;
import org.springframework.lang.NonNull;

public interface CrudRepository<T> {

	void save(@NonNull T t);

	T findById(long id);

	Collection<T> findAll();

	void delete(long id);

}
