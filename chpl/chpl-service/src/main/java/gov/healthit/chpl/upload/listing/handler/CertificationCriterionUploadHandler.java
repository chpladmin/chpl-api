package gov.healthit.chpl.upload.listing.handler;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Component("certificationCriterionUploadHandler")
@Log4j2
public class CertificationCriterionUploadHandler {
    private CertificationCriterionService criteriaService;

    @Autowired
    public CertificationCriterionUploadHandler(CertificationCriterionService criteriaService) {
        this.criteriaService = criteriaService;
    }

    public CertificationCriterion handle(CSVRecord certHeadingRecord) {
        CertificationCriterion criterion = null;
        String criterionNumberHeading = certHeadingRecord.get(0);
        criterion = criteriaService.getCriterionForHeading(criterionNumberHeading);

        if (criterion == null) {
            LOGGER.error("Unable to find a CertificationCriterion for heading " + criterionNumberHeading);
        }

        return criterion;
    }
}
