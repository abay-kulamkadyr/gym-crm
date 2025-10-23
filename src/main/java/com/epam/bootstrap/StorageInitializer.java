package com.epam.bootstrap;

import java.util.Map;

public interface StorageInitializer<T> {

	void initialize(Map<Long, T> storage);

	String getStorageBeanName();

}