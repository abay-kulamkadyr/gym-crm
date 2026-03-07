package com.epam.integration.base;

import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@Transactional // rolls back every test automatically
@Sql(
        scripts = {"/truncate.sql", "/seed_training_types.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public abstract class TransactionalTestBase extends IntegrationTestBase {}
