package com.epam.infrastructure.bootstrap;

import java.util.Map;

interface StorageInitializer<T> {

	void initialize(Map<Long, T> storage);

}