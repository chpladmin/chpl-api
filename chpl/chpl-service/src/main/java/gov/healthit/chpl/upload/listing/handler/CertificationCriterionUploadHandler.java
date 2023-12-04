package gov.healthit.chpl.upload.listing.handler;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Component("certificationCriterionUploadHandler")
@Log4j2
public class CertificationCriterionUploadHandler {
    private CertificationCriterionDAO criteriaDao;
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public CertificationCriterionUploadHandler(CertificationCriterionDAO criteriaDao,
            ListingUploadHandlerUtil uploadUtil) {
        this.criteriaDao = criteriaDao;
        this.uploadUtil = uploadUtil;
    }

    public CertificationCriterion handle(CSVRecord certHeadingRecord, CertifiedProductSearchDetails listing) {
        LocalDate certificationDate = getCertificationDateFromListing(listing);

        CertificationCriterion criterion = null;
        String criterionNumberHeading = certHeadingRecord.get(0);
        String criterionNumber = uploadUtil.parseCriteriaNumberFromHeading(criterionNumberHeading);
        boolean isCures = uploadUtil.isCriteriaNumberHeadingCures(criterionNumberHeading);
        if (criterionNumber != null && isCures) {
            criterion = getCuresCriterion(criterionNumber);
        } else if (criterionNumber != null) {
            criterion = getActiveCriterion(criterionNumber, certificationDate);
        } else {
            LOGGER.warn("Unable to find a criteria for the header value " + certHeadingRecord.toString());
        }

        return criterion;
    }

    private CertificationCriterion getCuresCriterion(String criterionNumber) {
        List<CertificationCriterion> criteriaWithNumber = criteriaDao.getAllByNumber(criterionNumber);
        if (criteriaWithNumber == null || criteriaWithNumber.size() == 0) {
            LOGGER.warn("Could not find a certification criterion matching " + criterionNumber);
            return null;
        }

        Optional<CertificationCriterion> criterionOpt = null;
        if (criteriaWithNumber.size() == 1) {
            criterionOpt = Optional.of(criteriaWithNumber.get(0));
        } else {
            //We used to check the matching criteria titles for the Cures Update keywords but no longer
            //have that. To ensure we are getting the Cures criterion, I think we can just pick the one
            //with the most recent starting date?
            criterionOpt = criteriaWithNumber.stream()
                    .max((o1, o2) -> o1.getStartDay().compareTo(o2.getStartDay()));
        }

        if (criterionOpt == null || !criterionOpt.isPresent()) {
            LOGGER.warn("Could not find Cures certification criterion matching " + criterionNumber);
            return null;
        }
        return criterionOpt.get();
    }

    private CertificationCriterion getActiveCriterion(String criterionNumber, LocalDate listingCertificationDate) {
        LocalDate criteriaActiveOnDate = (listingCertificationDate != null ? listingCertificationDate : LocalDate.now());
        List<CertificationCriterion> criteriaWithNumber = criteriaDao.getAllByNumber(criterionNumber);
        if (criteriaWithNumber == null || criteriaWithNumber.size() == 0) {
            LOGGER.warn("Could not find a certification criterion matching " + criterionNumber);
            return null;
        }

        Optional<CertificationCriterion> criterionOpt = null;
        if (criteriaWithNumber.size() == 1) {
            criterionOpt = Optional.of(criteriaWithNumber.get(0));
        } else  {
            criterionOpt = criteriaWithNumber.stream()
                    .filter(criterion -> (criterion.getStartDay().isBefore(criteriaActiveOnDate)
                            || criterion.getStartDay().isEqual(criteriaActiveOnDate))
                            && (criterion.getEndDay() == null
                            || criterion.getEndDay().isEqual(criteriaActiveOnDate)
                            || criterion.getEndDay().isAfter(criteriaActiveOnDate)))
                    .findFirst();
            //if there was no active criteria, print out a message and just choose the first criteria
            //the user will get an error about it during validation since the criteria dates
            //won't overlap the listing certification date
            if (criterionOpt == null || !criterionOpt.isPresent()) {
                LOGGER.warn("Could not find a certification criteria active on " + criteriaActiveOnDate + " matching " + criterionNumber);
                criterionOpt = Optional.of(criteriaWithNumber.get(0));
            }
        }

        if (criterionOpt == null || !criterionOpt.isPresent()) {
            LOGGER.warn("Could not find a certification criterion " + criteriaActiveOnDate + " matching " + criterionNumber);
            return null;
        }
        return criterionOpt.get();
    }

    private LocalDate getCertificationDateFromListing(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationDate() != null) {
            return DateUtil.toLocalDate(listing.getCertificationDate());
        }
        return null;
    }
}
