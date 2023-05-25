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
import gov.healthit.chpl.subscription.service.SubscriptionLookupUtil;
import gov.healthit.chpl.subscription.subject.processor.CertificationCriteriaAddedActivityProcessor;
import gov.healthit.chpl.subscription.subject.processor.CertificationStatusChangedActivityProcessor;
import gov.healthit.chpl.subscription.subject.processor.SubscriptionSubjectProcessor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class SubscriptionObserver {
    private SubscriptionDao subscriptionDao;
    private SubscriptionObservationDao observationDao;
    private SubscriptionLookupUtil lookupUtil;

    private List<SubscriptionSubjectProcessor> processors;
    private List<SubscriptionSubject> allSubjects;

    @Autowired
    public SubscriptionObserver(SubscriptionDao subscriptionDao,
            SubscriptionObservationDao observationDao,
            SubscriptionLookupUtil lookupUtil) {
        this.subscriptionDao = subscriptionDao;
        this.observationDao = observationDao;
        this.lookupUtil = lookupUtil;
        this.allSubjects = subscriptionDao.getAllSubjects();

        createSubscriptionSubjectProcessors();
    }

    public void checkActivityForSubscriptions(ActivityDTO activity, Object originalData, Object newData) {
        processors.stream()
            .filter(processor -> processor.isRelevantTo(activity, originalData, newData))
            .forEach(processor -> createObservations(processor.getSubject().getId(), activity.getActivityObjectId(), activity.getId()));
    }

    private void createObservations(Long subjectId, Long objectId, Long activityId) {
        List<Long> subscriptionIds = subscriptionDao.getSubscriptionIdsForConfirmedSubscribers(subjectId, objectId);
        if (!CollectionUtils.isEmpty(subscriptionIds)) {
            observationDao.createObservations(subscriptionIds, activityId);
        }
    }

    private SubscriptionSubject getSubject(Long subjectId) {
        Optional<SubscriptionSubject> subjectWithId = allSubjects.stream()
                .filter(subj -> subj.getId().equals(subjectId))
                .findAny();
        if (subjectWithId.isEmpty()) {
            LOGGER.error("Expected a subscription subject with ID '" + subjectId + "' but it was not found.");
            return null;
        }
        return subjectWithId.get();
    }

    private void createSubscriptionSubjectProcessors() {
        this.processors = new ArrayList<SubscriptionSubjectProcessor>();
        this.processors.add(new CertificationStatusChangedActivityProcessor(getSubject(lookupUtil.getCertificationStatusChangedSubjectId())));
        this.processors.add(new CertificationCriteriaAddedActivityProcessor(getSubject(lookupUtil.getCertificationCriteriaAddedSubjectId())));
    }
}
