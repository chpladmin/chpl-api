package gov.healthit.chpl.scheduler;

import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * A Spring singleton object object that provides a reference to the Quartz Scheduler.
 * @author TYoung
 *
 */
@Scope(value = "singleton")
@Component
public class ChplSchedulerReference {
    @Autowired
    private SchedulerFactoryBean schedulerFactory;

    private Scheduler scheduler;

    /**
     * Initialize the scheduler service
     */
    @PostConstruct
    private void init() {
      scheduler = schedulerFactory.getScheduler();
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
