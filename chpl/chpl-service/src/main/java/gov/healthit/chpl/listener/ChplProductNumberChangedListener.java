package gov.healthit.chpl.listener;

import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductChplProductNumberHistoryDao;
import gov.healthit.chpl.domain.CertifiedProductChplProductNumberHistory;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ChplProductNumberChangedListener {
    private CertifiedProductChplProductNumberHistoryDao chplProductNumberHistoryDao;

    @Autowired
    public ChplProductNumberChangedListener(@Qualifier("certifiedProductChplProductNumberHistoryDao")
        CertifiedProductChplProductNumberHistoryDao chplProductNumberHistoryDao) {
        this.chplProductNumberHistoryDao = chplProductNumberHistoryDao;
    }

    public void recordChplProductNumberChanged(ActivityConcept concept, Long objectId,
            Object originalData, Object newData, Date activityDate) throws EntityCreationException {
        if (isListingUpdateActivity(concept, originalData, newData)) {
            CertifiedProductSearchDetails originalListing = (CertifiedProductSearchDetails) originalData;
            CertifiedProductSearchDetails newListing = (CertifiedProductSearchDetails) newData;
            if (chplProductNumberChanged(originalListing, newListing)) {
                LOGGER.info("CHPL product number for listing ID " + objectId + " changed from "
                        + originalListing.getChplProductNumber() + " to "
                        + newListing.getChplProductNumber());
                chplProductNumberHistoryDao.createChplProductNumberHistoryMapping(objectId,
                        CertifiedProductChplProductNumberHistory.builder()
                            .chplProductNumber(originalListing.getChplProductNumber())
                            .endDateTime(DateUtil.toLocalDateTime(activityDate.getTime()))
                        .build());
            }
        }
    }

    private boolean isListingUpdateActivity(ActivityConcept concept, Object originalData, Object newData) {
        return ObjectUtils.allNotNull(concept, newData, originalData)
                && ActivityConcept.CERTIFIED_PRODUCT.equals(concept)
                && originalData instanceof CertifiedProductSearchDetails
                && newData instanceof CertifiedProductSearchDetails;
    }

    private boolean chplProductNumberChanged(CertifiedProductSearchDetails originalListing, CertifiedProductSearchDetails newListing) {
        return !Objects.equals(originalListing.getChplProductNumber(), newListing.getChplProductNumber());
    }
}
