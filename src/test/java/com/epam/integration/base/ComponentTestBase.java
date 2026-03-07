package com.epam.integration.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql(
        scripts = {"/truncate.sql", "/seed_training_types.sql", "/seed_users.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class ComponentTestBase extends IntegrationTestBase {
    @Autowired
    protected TestRestTemplate testRestTemplate;
}
