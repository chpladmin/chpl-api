package gov.healthit.chpl.scheduler.job.onetime.questionableActivity;

import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.questionableactivity.QuestionableActivityDAO;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityTriggerDTO;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "fixupQuestionableActivityJobLogger")
public class FixupQuestionableActivity  implements Job {

    @Autowired
    private QuestionableActivityDAO questionableActivityDao;

    private List<QuestionableActivityTriggerDTO> triggerTypes;

    @Autowired
    @Qualifier("updatableQuestionableActivityDao")
    private UpdatableQuestionableActivityDao updatableQuestionableActivityDao;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Fixup Questionable Activity job. *********");
        triggerTypes = questionableActivityDao.getAllTriggers();

        updatableQuestionableActivityDao.deleteQuestionableActivityForTrigger(
                getTrigger(QuestionableActivityTriggerConcept.CRITERIA_ADDED));


        LOGGER.info("********* Completed the Fixup Questionable Activity job. *********");
    }

    private QuestionableActivityTriggerDTO getTrigger(QuestionableActivityTriggerConcept trigger) {
        QuestionableActivityTriggerDTO result = null;
        for (QuestionableActivityTriggerDTO currTrigger : triggerTypes) {
            if (trigger.getName().equalsIgnoreCase(currTrigger.getName())) {
                result = currTrigger;
            }
        }
        return result;
    }
}
