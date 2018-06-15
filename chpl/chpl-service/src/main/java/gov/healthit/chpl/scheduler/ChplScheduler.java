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
     * @param args if first parameter == "init", initialize scheduler using properties file
     * that will erase existing triggers. Otherwise do not touch triggers
     */
    public static void main(final String[] args) {
        try {
            StdSchedulerFactory sf = new StdSchedulerFactory();
            if (args != null && args.length == 1 && args[0].equalsIgnoreCase("init")) {
                sf.initialize("quartz.init.properties");
            } else {
                sf.initialize("quartz.properties");
            }
            Scheduler scheduler = sf.getScheduler();
            scheduler.start();
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }
}
