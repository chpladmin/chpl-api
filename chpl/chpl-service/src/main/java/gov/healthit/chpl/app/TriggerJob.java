package gov.healthit.chpl.app;

import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;
import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.util.StringUtils;

/**
 * Used to manually trigger a job that otherwise is on a cron schedule.
 * @author alarned
 *
 */
public final class TriggerJob {
    private static final Logger LOGGER = LogManager.getLogger(TriggerJob.class);
    private TriggerJob() { }
    private static DateTimeFormatter dateTimeFormatter;

    /**
     * Triggers a job to run once.
     * @param args if no arguments are specified, returns all system jobs,
     *  otherwise takes action based on first argument
     */
    public static void main(final String[] args) {
        dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
        if (args != null && args.length > 0) {
            switch (args[0]) {
            case "start":
                startJob(Arrays.copyOfRange(args, 1, args.length));
                break;
            case "list":
                listJobs();
                break;
            case "interrupt":
                interruptJob(Arrays.copyOfRange(args, 1, args.length));
                break;
            case "help":
                displayHelp(args[0]);
                break;
            default:
                displayHelp();
                break;
            }
        } else {
            displayHelp();
        }
    }

    private static void displayHelp() {
        LOGGER.info("Expects 0 or more arguments."
                + "\n   0 arguments: display this information"
                + "\n   1st argument: one of \"list\" \"start\" \"interrupt\" \"help\" to cause relevant action"
                + "\n   nth arguments: action specific arguments");
    }

    private static void displayHelp(final String action) {
        if (action != null) {
            switch (action) {
            case "start":
                LOGGER.info("Start command expects one or two arguments."
                        + "\n   1st argument: job name"
                        + "\n   2nd argument: group name (defaults to \"systemJobs\" if not provided and if un/pw not provided)"
                        + "\n   3rd argument: username"
                        + "\n   4th argument: password");
                break;
            case "list":
                LOGGER.info("List command expects no arguments."
                        + "\n   Outputs currently available triggers and running Triggers");
                break;
            case "interrupt":
                LOGGER.info("Interrupt command expects one or two arguments."
                        + "\n   1st argument: job name"
                        + "\n   2nd argument: group name (defaults to \"systemJobs\" if not provided)");
                break;
            case "help":
                LOGGER.info("Help command expects zero or one argument."
                        + "\n   No arguments: display this help"
                        + "\n   One argument: action name, display help for that action"
                        + "\n   Available actions: start list interrupt help");
                break;
            default:
                displayHelp();
                break;
            }
        } else {
            displayHelp();
        }
    }

    private static void interruptJob(final String[] args) {
        String jobName = null;
        String jobGroup = "systemJobs";
        switch (args.length) {
        case 2:
            jobGroup = args[1];
        case 1:
            jobName = args[0];
            break;
        default:
            LOGGER.error("Interrupt command expects one or two arguments."
                    + "\n   1st argument: job name"
                    + "\n   2nd argument: group name (defaults to \"systemJobs\" if not provided)");
            System.exit(1);
        }
        try {
            StdSchedulerFactory sf = new StdSchedulerFactory();
            sf.initialize();
            Scheduler scheduler = sf.getScheduler();

            TriggerKey triggerId = triggerKey("interruptJob_" + new Date().getTime(), "interruptJobTrigger");
            JobKey jobId = jobKey("interruptJob", "systemJobs");

            Trigger qzTrigger = newTrigger()
                    .withIdentity(triggerId)
                    .startNow()
                    .forJob(jobId)
                    .usingJobData("jobName", jobName)
                    .usingJobData("jobGroup", jobGroup)
                    .build();
            scheduler.scheduleJob(qzTrigger);
            scheduler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private static void listJobs() {
        try {
            StdSchedulerFactory sf = new StdSchedulerFactory();
            sf.initialize();
            Scheduler scheduler = sf.getScheduler();

            StringBuilder output = new StringBuilder();
            output.append("Found jobs:\n");
            for (String group: scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(groupEquals(group))) {
                    output.append("    Job: [" + jobKey.getName() + "] Group: [" + jobKey.getGroup() + "]\n");
                }
            }
            output.append(scheduler.getCurrentlyExecutingJobs().size() + " running job(s):\n");

            for (JobExecutionContext executing : scheduler.getCurrentlyExecutingJobs()) {
                output.append("    Job: [" + executing.getJobDetail().getKey().getName()
                        + "] Group [" + executing.getJobDetail().getKey().getGroup() + "]\n");
            }
            scheduler.shutdown();
            LOGGER.info(output.toString());
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    private static void startJob(final String[] args) {
        String jobName = null;
        String jobGroup = "systemJobs";
        String username = "";
        String password = "";
        switch (args.length) {
        case 4:
            password = args[3];
        case 3:
            username = args[2];
        case 2:
            jobGroup = args[1];
        case 1:
            jobName = args[0];
            break;
        default:
            LOGGER.error("Start command expects one to four arguments."
                    + "\n   1st argument: job name"
                    + "\n   2nd argument: group name (defaults to \"systemJobs\" if not provided, required if un/pw is provided)"
                    + "\n   3rd argument: username"
                    + "\n   4th argument: password");
            System.exit(1);
        }
        try {
            LOGGER.info("Starting job {} in group {}", jobName, jobGroup);
            StdSchedulerFactory sf = new StdSchedulerFactory();
            sf.initialize();
            Scheduler scheduler = sf.getScheduler();

            TriggerKey triggerId = triggerKey("triggerJobNow_" + new Date().getTime(), "triggerJob");
            JobKey jobId = jobKey(jobName, jobGroup);

            Trigger qzTrigger = newTrigger()
                    .withIdentity(triggerId)
                    .startNow()
                    .forJob(jobId)
                    .build();
            if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
                qzTrigger.getJobDataMap().put("username", username);
                qzTrigger.getJobDataMap().put("password", password);
            }
            scheduler.scheduleJob(qzTrigger);
            scheduler.shutdown();
            LOGGER.info("Expecting job to start at {}", (dateTimeFormatter
                    .format(LocalDateTime.ofInstant(Instant.ofEpochMilli(qzTrigger.getNextFireTime().getTime()),
                            ZoneId.systemDefault()))));
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }
}
