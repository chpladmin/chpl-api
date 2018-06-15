package gov.healthit.chpl.scheduler;

import javax.naming.spi.NamingManager;

/**
 * Static class used to create a local context for Quartz schedules.
 * Do not instantiate this class directly. Use the factory method.
 * @author alarned
 *
 */
public final class QuartzLocalContextFactory {

    private QuartzLocalContextFactory() {
    }

    /**
     * Set initial context factory builder if it's not already set.
     * @param databaseDriver database info
     * @return a local context
     * @throws Exception if issue
     */
    public static QuartzLocalContext createLocalContext(final String databaseDriver) throws Exception {

        try {
            QuartzLocalContext ctx = new QuartzLocalContext();
            Class.forName(databaseDriver);
            if (!NamingManager.hasInitialContextFactoryBuilder()) {
                NamingManager.setInitialContextFactoryBuilder(ctx);
            }
            return ctx;
        } catch (Exception e) {
            throw new Exception("Error Initializing Context: " + e.getMessage(), e);
        }
    }
}
