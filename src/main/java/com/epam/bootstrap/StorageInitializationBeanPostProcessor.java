package com.epam.bootstrap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class StorageInitializationBeanPostProcessor implements BeanPostProcessor {

	private final Map<String, StorageInitializer<?>> initializersByBeanName;

	@Autowired
	public StorageInitializationBeanPostProcessor(List<StorageInitializer<?>> initializers) {
		this.initializersByBeanName = initializers.stream()
			.collect(Collectors.toMap(StorageInitializer::getStorageBeanName, Function.identity()));
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		StorageInitializer<?> initializer = initializersByBeanName.get(beanName);

		if (initializer != null) {
			@SuppressWarnings("unchecked")
			Map<Long, ?> storage = (Map<Long, ?>) bean;
			initializeStorageSafely(initializer, storage);
		}

		return bean;
	}

	@SuppressWarnings("unchecked")
	private <T> void initializeStorageSafely(StorageInitializer<T> initializer, Map<Long, ?> storage) {
		initializer.initialize((Map<Long, T>) storage);
	}

}
