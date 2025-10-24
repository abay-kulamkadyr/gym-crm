package com.epam.infrastructure.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a storage bean or initializer component associated with a specific entity type.
 * <p>
 * Used to link:
 * <ul>
 * <li>A storage {@code Map} bean (via {@code @Bean})</li>
 * <li>An initializer component (via {@code @Component})</li>
 * <li>The entity type they handle</li>
 * </ul>
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InitializableStorage {

	Class<?> entityType();

}
