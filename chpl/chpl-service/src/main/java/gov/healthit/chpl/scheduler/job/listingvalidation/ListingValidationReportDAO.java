package gov.healthit.chpl.scheduler.job.listingvalidation;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository
public class ListingValidationReportDAO extends BaseDAOImpl {

    public ListingValidationReport create(ListingValidationReport lvr) {
        ListingValidationReportEntity entity = ListingValidationReportEntity.builder()
                .chplProductNumber(lvr.getChplProductNumber())
                .productName(lvr.getProductName())
                .certificationStatusName(lvr.getCertificationStatusName())
                .errorMessage(lvr.getErrorMessage())
                .reportDate(lvr.getReportDate())
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        return new ListingValidationReport(entity);
    }

}
