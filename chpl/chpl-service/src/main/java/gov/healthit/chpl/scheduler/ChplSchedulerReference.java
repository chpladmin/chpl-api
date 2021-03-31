package gov.healthit.chpl.scheduler;

import javax.annotation.PostConstruct;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * A Spring singleton object object that provides a reference to the Quartz Scheduler.
 * @author TYoung
 *
 */
@Scope(value = "singleton")
@Component
public class ChplSchedulerReference {
    private Scheduler scheduler;

    /**
     * Initializes the Quartz Scheduler when this object is created.
     * @throws SchedulerException if thrown
     */
    @PostConstruct
    public void init() throws SchedulerException {
        StdSchedulerFactory sf = new StdSchedulerFactory();
        sf.initialize();
        this.scheduler = sf.getScheduler();
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
