package gov.healthit.chpl.scheduler.job.subscriptions.subjects.formatter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import gov.healthit.chpl.subscription.subject.processor.ServiceBaseUrlListChangedActivityProcessor;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "subscriptionObservationsNotificationJobLogger")
public class ServiceBaseUrlListChangedFormatter extends ObservationSubjectFormatter {
    private static final String DESCRIPTION_REMOVED_UNFORMATTED = "Service Base URL List was removed from %s.";
    private static final String DESCRIPTION_ADDED_UNFORMATTED = "Service Base URL List %s was added to %s.";
    private static final String DESCRIPTION_UPDATED_UNFORMATTED = "Service Base URL List was changed from %s to %s on %s";

    @Autowired
    public ServiceBaseUrlListChangedFormatter(@Qualifier("activityDAO") ActivityDAO activityDao,
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

        CertificationResult originalG10CertResult = getG10CertResult(before);
        CertificationResult newG10CertResult = getG10CertResult(after);

        List<List<String>> formattedObservations = new ArrayList<List<String>>();
        if (!StringUtils.isEmpty(originalG10CertResult.getServiceBaseUrlList()) && StringUtils.isEmpty(newG10CertResult.getServiceBaseUrlList())) {
            formattedObservations.add(Stream.of(
                    String.format(DESCRIPTION_REMOVED_UNFORMATTED, Util.formatCriteriaNumber(newG10CertResult.getCriterion())))
                    .toList());
        } else if (StringUtils.isEmpty(originalG10CertResult.getServiceBaseUrlList()) && !StringUtils.isEmpty(newG10CertResult.getServiceBaseUrlList())) {
            formattedObservations.add(Stream.of(
                    String.format(DESCRIPTION_ADDED_UNFORMATTED, newG10CertResult.getServiceBaseUrlList(), Util.formatCriteriaNumber(newG10CertResult.getCriterion())))
                    .toList());
        } else {
            String updatedObservation = String.format(DESCRIPTION_UPDATED_UNFORMATTED,
                    originalG10CertResult.getServiceBaseUrlList(),
                    newG10CertResult.getServiceBaseUrlList(),
                    Util.formatCriteriaNumber(newG10CertResult.getCriterion()));
            formattedObservations.add(Stream.of(updatedObservation).toList());
        }
        return formattedObservations;
    }


    private CertificationResult getG10CertResult(CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion() != null
                    && certResult.getCriterion().getId().equals(ServiceBaseUrlListChangedActivityProcessor.G10_CRITERION_ID))
                .findAny().orElse(null);
    }
}
