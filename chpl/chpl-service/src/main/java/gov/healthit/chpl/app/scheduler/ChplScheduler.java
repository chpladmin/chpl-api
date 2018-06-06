package gov.healthit.chpl.app.scheduler;

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
     * Starts the scheduler. Scheduler uses properties files and XML to determine behavior.
     * @param args no arguments are supported
     */
    public static void main(final String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }
}
