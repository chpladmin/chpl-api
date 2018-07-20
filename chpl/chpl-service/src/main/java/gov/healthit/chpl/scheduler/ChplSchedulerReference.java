package gov.healthit.chpl.scheduler;

import javax.annotation.PostConstruct;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope(value = "singleton")
@Component
public class ChplSchedulerReference {
    private Scheduler scheduler;
    
    @PostConstruct
    public void init() throws SchedulerException {
        StdSchedulerFactory sf = new StdSchedulerFactory();
        sf.initialize("quartz.properties");
        setScheduler(sf.getScheduler());
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
