package com.epam.application.service;

import java.util.Optional;

public interface CrudService<T> {

	void create(T t);

	void update(T t);

	void delete(Long id);

	Optional<T> getById(Long id);

}
