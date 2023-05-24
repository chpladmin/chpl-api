package gov.healthit.chpl.subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.subscription.dao.SubscriptionDao;
import gov.healthit.chpl.subscription.dao.SubscriptionObservationDao;
import gov.healthit.chpl.subscription.domain.SubscriptionSubject;
import gov.healthit.chpl.subscription.subject.processor.CertificationStatusChangedActivityProcessor;
import gov.healthit.chpl.subscription.subject.processor.SubscriptionSubjectProcessor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class SubscriptionObserver {
    private SubscriptionDao subscriptionDao;
    private SubscriptionObservationDao observationDao;

    private List<SubscriptionSubjectProcessor> processors;
    private List<SubscriptionSubject> allSubjects;

    @Autowired
    public SubscriptionObserver(SubscriptionDao subscriptionDao,
            SubscriptionObservationDao observationDao) {
        this.subscriptionDao = subscriptionDao;
        this.observationDao = observationDao;
        this.allSubjects = subscriptionDao.getAllSubjects();

        createSubscriptionSubjectProcessors();
    }

    public void checkActivityForSubscriptions(ActivityDTO activity, Object originalData, Object newData) {
        processors.stream()
            .filter(processor -> processor.isRelevantTo(activity, originalData, newData))
            .map(processor -> getSubjectId(processor.getSubjectName()))
            .filter(subjectId -> subjectId != null)
            .forEach(subjectId -> createObservations(subjectId, activity.getActivityObjectId(), activity.getId()));
    }

    private Long getSubjectId(String subjectName) {
        Optional<SubscriptionSubject> subjectWithName = allSubjects.stream()
                .filter(subj -> subj.getSubject().equals(subjectName))
                .findAny();
        if (subjectWithName.isEmpty()) {
            LOGGER.error("Expected a subscription subject '" + subjectName + "' but it was not found.");
            return null;
        }
        return subjectWithName.get().getId();
    }

    private void createObservations(Long subjectId, Long objectId, Long activityId) {
        List<Long> subscriptionIds = subscriptionDao.getSubscriptionIdsForConfirmedSubscribers(subjectId, objectId);
        if (!CollectionUtils.isEmpty(subscriptionIds)) {
            observationDao.createObservations(subscriptionIds, activityId);
        }
    }

    private void createSubscriptionSubjectProcessors() {
        this.processors = new ArrayList<SubscriptionSubjectProcessor>();
        this.processors.add(new CertificationStatusChangedActivityProcessor());
        //TODO: Add processors in the future for other actions we care about
    }
}
