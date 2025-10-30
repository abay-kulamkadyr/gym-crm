package com.epam.application.service;

import java.util.Optional;

public interface CrudService<T> {

	void create(T t);

	void update(T t);

	void delete(long id);

	Optional<T> getById(long id);

}
