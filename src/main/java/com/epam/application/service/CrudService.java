package com.epam.application.service;

import java.util.Collection;

public interface CrudService<T> {

	void create(T t);

	void update(T t);

	void delete(long id);

	T getById(long id);

	Collection<T> getAll();

}
