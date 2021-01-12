package gov.healthit.chpl.scheduler.job.listingvalidation;

import java.util.Date;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
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
                .creationDate(new Date())
                .lastModifiedUser(User.SYSTEM_USER_ID)
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        return new ListingValidationReport(entity);
    }

}
