package com.epam.integration.base;

import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Sql(
        scripts = {"/truncate.sql", "/seed_training_types.sql", "/seed_users.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public abstract class SeededIntegrationTestBase extends IntegrationTestBase {}
