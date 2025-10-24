package com.epam.infrastructure.bootstrap;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.type.MethodMetadata;
import org.springframework.stereotype.Component;

/**
 * * A custom Spring {@link org.springframework.beans.factory.config.BeanPostProcessor}
 * responsible for automatically initializing {@link java.util.Map} beans that are
 * intended to serve as in-memory storage for domain entities. *
 * <p>
 * This processor works by inspecting the {@code @Bean} factory method for the presence of
 * the {@code @InitializableStorage} annotation. If the annotation is found, the component
 * locates the corresponding {@link com.epam.infrastructure.bootstrap.StorageInitializer}
 * and uses it to populate the newly created Map bean with initial data.
 * </p>
 * *
 * <p>
 * It requires the custom {@link com.epam.infrastructure.bootstrap.InitializableStorage}
 * annotation and implementations of the
 * {@link com.epam.infrastructure.bootstrap.StorageInitializer} interface to function
 * correctly.
 * </p>
 */
@Component
@Slf4j
class StorageInitializationBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

	private final Map<Class<?>, StorageInitializer<?>> initializersByType;

	private ConfigurableListableBeanFactory beanFactory;

	@Autowired
	public StorageInitializationBeanPostProcessor(List<StorageInitializer<?>> initializers) {
		// build a map that: {
		// Trainee.class -> TraineeStorageInitializer,
		// Trainer.class -> TrainerStorageInitializer,
		// Training.class -> TrainingStorageInitializer,
		// TrainingType.class -> TrainingTypeStorageInitializer
		// }
		this.initializersByType = new HashMap<>();

		for (StorageInitializer<?> initializer : initializers) {
			InitializableStorage annotation = initializer.getClass().getAnnotation(InitializableStorage.class);

			if (annotation != null) {
				initializersByType.put(annotation.entityType(), initializer);
				log.info("Registered initializer for type: {}", annotation.entityType().getSimpleName());
			}
		}
	}

	// -------------------------------------------------------------------------
	// Spring Lifecycle Methods
	// -------------------------------------------------------------------------

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		// for accessing bean metadata
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (!(bean instanceof Map)) {
			return bean;
		}

		// Try to get the annotation from the bean definition (factory method)
		InitializableStorage annotation = findStorageAnnotation(beanName);

		if (annotation == null) {
			return bean;
		}

		StorageInitializer<?> initializer = initializersByType.get(annotation.entityType());

		if (initializer == null) {
			log.warn("No initializer found for entity type: {}", annotation.entityType().getSimpleName());
			return bean; // Annotated, but no corresponding initializer registered
		}

		@SuppressWarnings("unchecked")
		Map<Long, ?> storage = (Map<Long, ?>) bean;

		log.info("Initializing storage '{}' for type: {}", beanName, annotation.entityType().getSimpleName());
		initializeStorageSafely(initializer, storage);
		log.info("Storage '{}' initialized with {} entries", beanName, storage.size());

		return bean;
	}

	// -------------------------------------------------------------------------
	// Private Helper Methods
	// -------------------------------------------------------------------------

	/**
	 * Attempts to find and extract the @InitializableStorage annotation metadata from a
	 * bean defined by a @Bean factory method.
	 */
	private InitializableStorage findStorageAnnotation(String beanName) {
		final String annotationClassName = InitializableStorage.class.getName();

		try {
			BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

			// Guard Clause 1: Must be an AnnotatedBeanDefinition (from a @Bean method)
			if (!(beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDef)) {
				return null;
			}

			MethodMetadata factoryMethodMetadata = annotatedBeanDef.getFactoryMethodMetadata();

			// Guard Clause 2: Must have a factory method and be annotated with our
			// annotation
			if (factoryMethodMetadata == null || !factoryMethodMetadata.isAnnotated(annotationClassName)) {
				return null;
			}

			// Extract annotation attributes
			Map<String, Object> attributes = factoryMethodMetadata.getAnnotationAttributes(annotationClassName);

			// Guard Clause 3: Must have attributes and specifically the "entityType"
			// attribute
			if (attributes == null || !attributes.containsKey("entityType")) {
				return null;
			}

			// Extract and cast the entityType
			Class<?> entityType = (Class<?>) attributes.get("entityType");

			log.debug("Found @InitializableStorage on bean '{}' with entityType: {}", beanName,
					entityType.getSimpleName());

			return createAnnotationProxy(entityType);

		}
		catch (Exception e) {
			// Catches NoSuchBeanDefinitionException, etc.
			log.debug("Could not find annotation metadata for bean: {}", beanName, e);
		}

		return null;
	}

	/**
	 * Creates a proxy implementation of the InitializableStorage annotation since we
	 * can't instantiate annotations directly.
	 */
	private InitializableStorage createAnnotationProxy(final Class<?> entityType) {
		return new InitializableStorage() {
			@Override
			public Class<?> entityType() {
				return entityType;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return InitializableStorage.class;
			}
		};
	}

	@SuppressWarnings("unchecked")
	private <T> void initializeStorageSafely(StorageInitializer<T> initializer, Map<Long, ?> storage) {
		initializer.initialize((Map<Long, T>) storage);
	}

}