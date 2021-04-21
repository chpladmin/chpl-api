package gov.healthit.chpl.scheduler;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Schedule application.
 * @author alarned
 *
 */
public final class ChplScheduler {
    private ChplScheduler() { }

    /**
     * Starts the scheduler. Scheduler uses various properties files and XML to determine behavior.
     * @param args no arguments are expected
     */
    public static void main(final String[] args) {
        try {
            StdSchedulerFactory sf = new StdSchedulerFactory();
            sf.initialize();
            Scheduler scheduler = sf.getScheduler();
            scheduler.start();
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }
}
