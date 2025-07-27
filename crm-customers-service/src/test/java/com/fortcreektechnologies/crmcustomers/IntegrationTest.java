package com.fortcreektechnologies.crmcustomers;

import com.fortcreektechnologies.crmcustomers.config.AsyncSyncConfiguration;
import com.fortcreektechnologies.crmcustomers.config.EmbeddedRedis;
import com.fortcreektechnologies.crmcustomers.config.EmbeddedSQL;
import com.fortcreektechnologies.crmcustomers.config.JacksonConfiguration;
import com.fortcreektechnologies.crmcustomers.config.TestSecurityConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = { CrmCustomersServiceApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class, TestSecurityConfiguration.class }
)
@EmbeddedRedis
@EmbeddedSQL
public @interface IntegrationTest {
}
