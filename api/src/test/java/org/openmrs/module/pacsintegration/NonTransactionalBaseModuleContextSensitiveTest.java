package org.openmrs.module.pacsintegration;

import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

// TODO: figure out a better way of doing this--workaround for the fact that @NotTransactional has been deprecated

/**
 * Modules using the unit test framework should use this class instead of the general
 * {@link BaseContextSensitiveTest} one. Developers just need to make sure their modules are on the
 * classpath. The TestingApplicationContext.xml file tells spring/hibernate to look for and load all
 * modules found on the classpath. The ContextConfiguration annotation adds in the module
 * application context files to the config locations and the test application context (so that the
 * module services are loaded from the system classloader)
 */
@ContextConfiguration(locations = { "classpath:applicationContext-service.xml", "classpath*:TestingApplicationContext.xml",
        "classpath*:moduleApplicationContext.xml" }, inheritLocations = false)
public abstract class NonTransactionalBaseModuleContextSensitiveTest extends NonTransactionalBaseContextSensitiveTest {

}
