package com.epam.e2e;

import com.epam.integration.base.ComponentTestBase;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

@CucumberContextConfiguration
@Sql(
        scripts = {"/truncate.sql", "/seed_training_types.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS,
        config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
public class CucumberSpringConfiguration extends ComponentTestBase {}
