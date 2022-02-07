package gov.healthit.chpl;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.scheduler.ChplSchedulerReference;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SpringContextEventListener {

    private ChplSchedulerReference chplScheduler;

    @Autowired
    public SpringContextEventListener(ChplSchedulerReference chplScheduler) {
        this.chplScheduler = chplScheduler;
    }

    //If the Quartz scheduler needs to run a job upon server startup,
    //we don't want it to start until after the Spring context has been loaded
    //otherwise Autowired beans in those jobs may be null.
    @EventListener(classes = { ContextRefreshedEvent.class, ContextStartedEvent.class })
    public void handleContextEvent() {
        try {
            if (!this.chplScheduler.getScheduler().isStarted()) {
                this.chplScheduler.getScheduler().start();
            }
        } catch (SchedulerException ex) {
            LOGGER.fatal("Could not start CHPL Scheduler", ex);
        }
    }

}
