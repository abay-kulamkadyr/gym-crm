package com.epam.infrastructure.bootstrap;

import java.util.Map;

//Strategy interface
interface StorageInitializer<T> {

	void initialize(Map<Long, T> storage);

}