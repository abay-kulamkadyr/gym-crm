package com.epam.dao;

import java.util.Collection;
import java.util.Map;
import com.epam.domain.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class TrainerDao implements CrudRepository<Trainer> {

	private Map<Long, Trainer> storage;

	@Autowired
	public void setStorage(Map<Long, Trainer> storage) {
		this.storage = storage;
	}

	@Override
	public void save(@NonNull Trainer trainer) {
		storage.put(trainer.getUserId(), trainer);
	}

	@Override
	public Trainer findById(long id) {
		return storage.get(id);
	}

	@Override
	public Collection<Trainer> findAll() {
		return storage.values();
	}

	@Override
	public void delete(long id) {
		storage.remove(id);
	}

}
