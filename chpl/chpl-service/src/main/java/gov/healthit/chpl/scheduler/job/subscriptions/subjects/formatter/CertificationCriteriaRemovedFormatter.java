package gov.healthit.chpl.scheduler.job.subscriptions.subjects.formatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "subscriptionObservationsNotificationJobLogger")
public class CertificationCriteriaRemovedFormatter extends ObservationSubjectFormatter {
    private static final String DESCRIPTION_UNFORMATTED = "Certification criterion %s was unattested";

    @Autowired
    public CertificationCriteriaRemovedFormatter(@Qualifier("activityDAO") ActivityDAO activityDao,
            ListingActivityUtil listingActivityUtil) {
        super(activityDao, listingActivityUtil);
    }

    @Override
    public List<List<String>> toListsOfStrings(SubscriptionObservation observation) {
        ActivityDTO activity = getActivity(observation.getActivityId());

        CertifiedProductSearchDetails before = getListing(activity.getOriginalData());
        CertifiedProductSearchDetails after = getListing(activity.getNewData());

        if (before == null || after == null) {
            LOGGER.error("There was a problem turning activityID " + activity.getId() + " into listing details objects.");
            return null;
        }

        List<CertificationCriterion> removedCriteria = getRemovedCriteria(before, after);
        List<List<String>> formattedObservations = new ArrayList<List<String>>(removedCriteria.size());
        removedCriteria.stream()
            .forEach(criterion -> formattedObservations.add(Stream.of(observation.getSubscription().getSubject().getSubject(),
                    String.format(DESCRIPTION_UNFORMATTED, Util.formatCriteriaNumber(criterion)),
                    DateUtil.formatInEasternTime(activity.getActivityDate())).toList()));
        return formattedObservations;
    }

    private List<CertificationCriterion> getRemovedCriteria(CertifiedProductSearchDetails originalListing, CertifiedProductSearchDetails newListing) {
        List<Pair<CertificationResult, CertificationResult>> origAndNewCertResultPairs
            = originalListing.getCertificationResults().stream()
                .map(origCertResult -> createCertResultPair(origCertResult, newListing.getCertificationResults()))
                .collect(Collectors.toList());
        return origAndNewCertResultPairs.stream()
            .filter(pair -> (pair.getLeft() != null && BooleanUtils.isTrue(pair.getLeft().getSuccess()))
                                && (pair.getRight() == null || BooleanUtils.isFalse(pair.getRight().getSuccess())))
            .map(pair -> pair.getLeft().getCriterion())
            .collect(Collectors.toList());
    }

    private Pair<CertificationResult, CertificationResult> createCertResultPair(CertificationResult origCertResult, List<CertificationResult> newCertResults) {
        Optional<CertificationResult> newCertResult = newCertResults.stream()
                .filter(newCr -> newCr.getCriterion().getId().equals(origCertResult.getCriterion().getId()))
                .findAny();
        if (newCertResult.isEmpty()) {
            return Pair.of(origCertResult, null);
        }
        return Pair.of(origCertResult, newCertResult.get());
    }
}
