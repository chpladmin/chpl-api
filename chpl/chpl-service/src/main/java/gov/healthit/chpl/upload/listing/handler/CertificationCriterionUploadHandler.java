package gov.healthit.chpl.upload.listing.handler;

import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import lombok.extern.log4j.Log4j2;

@Component("certificationCriterionUploadHandler")
@Log4j2
public class CertificationCriterionUploadHandler {
    private static final String CURES_TITLE_KEY = "Cures Update";

    private CertificationCriterionDAO criteriaDao;
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public CertificationCriterionUploadHandler(CertificationCriterionDAO criteriaDao,
            ListingUploadHandlerUtil uploadUtil) {
        this.criteriaDao = criteriaDao;
        this.uploadUtil = uploadUtil;
    }

    public CertificationCriterion handle(CSVRecord certHeadingRecord) {
        CertificationCriterion criterion = null;
        String criterionNumberHeading = certHeadingRecord.get(0);
        String criterionNumber = uploadUtil.parseCriteriaNumberFromHeading(criterionNumberHeading);
        boolean isCures = uploadUtil.isCriteriaNumberHeadingCures(criterionNumberHeading);
        if (criterionNumber != null && isCures) {
            criterion = getCriterion(criterionNumber, isCures);
        } else if (criterionNumber != null && !isCures) {
            criterion = getCriterion(criterionNumber, isCures);
        } else {
            LOGGER.warn("Unable to find a criteria for the header value " + certHeadingRecord.toString());
        }
        return criterion;
    }

    private CertificationCriterion getCriterion(String criterionNumber, boolean isCures) {
        List<CertificationCriterionDTO> criteriaWithNumber = criteriaDao.getAllByNumber(criterionNumber);
        if (criteriaWithNumber == null || criteriaWithNumber.size() == 0) {
            LOGGER.warn("Could not find a certification criterion matching " + criterionNumber);
            return null;
        }

        Optional<CertificationCriterionDTO> criterionOpt = null;
        if (criteriaWithNumber.size() == 1) {
            criterionOpt = Optional.of(criteriaWithNumber.get(0));
        } else if (isCures) {
            criterionOpt = criteriaWithNumber.stream()
                    .filter(criterionWithNumber -> !StringUtils.isEmpty(criterionWithNumber.getTitle())
                    && criterionWithNumber.getTitle().contains(CURES_TITLE_KEY))
                    .findFirst();
        } else {
            criterionOpt = criteriaWithNumber.stream()
                    .filter(criterionWithNumber -> !StringUtils.isEmpty(criterionWithNumber.getTitle())
                    && !criterionWithNumber.getTitle().contains(CURES_TITLE_KEY))
                    .findFirst();
        }

        if (criterionOpt == null || !criterionOpt.isPresent()) {
            LOGGER.warn("Could not find a certification criterion (cures=" + isCures + ") matching " + criterionNumber);
            return null;
        }
        return new CertificationCriterion(criterionOpt.get());
    }
}
