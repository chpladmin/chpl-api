package gov.healthit.chpl.email;

import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.scheduler.ChplSchedulerReference;

@Component
public class ChplEmailFactory {
    private Environment env;
    private ChplSchedulerReference chplScheduler;

    @Autowired
    public ChplEmailFactory(ChplSchedulerReference chplSchedulerReference, Environment env) {
        this.chplScheduler = chplSchedulerReference;
        this.env = env;
    }

    public EmailBuilder emailBuilder() {
        return new EmailBuilder(env, getScheduler());
    }

    private Scheduler getScheduler() {
        return chplScheduler.getScheduler();
    }
}
