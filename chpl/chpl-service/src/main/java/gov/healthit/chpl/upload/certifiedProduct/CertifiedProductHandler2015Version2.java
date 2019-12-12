package gov.healthit.chpl.upload.certifiedProduct;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductParentListingEntity;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap2015Version2;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Adds ICS Source (family), Removes columns G1 and G2 for 170.315(g)(7),
 * Adds Test Tool + Test Tool Version + Test Data Version, Test Data Alteration, Test Data Alteration Description for criteria b8,
 * Removes Test Tool + Test Tool Version + Test Data fields for criteria f5.
 * @author kekey
 *
 */
@Component("certifiedProductHandler2015Version2")
public class CertifiedProductHandler2015Version2 extends CertifiedProductHandler2015Version1 {

    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductHandler2015Version2.class);
    private TemplateColumnIndexMap templateColumnIndexMap;

    @Autowired
    public CertifiedProductHandler2015Version2(final ErrorMessageUtil msgUtil) {
        super(msgUtil);
        templateColumnIndexMap = new TemplateColumnIndexMap2015Version2();
    }

    @Override
    public TemplateColumnIndexMap getColumnIndexMap() {
        return templateColumnIndexMap;
    }

    public PendingCertifiedProductEntity handle() throws InvalidArgumentsException {
        PendingCertifiedProductEntity pendingCertifiedProduct = super.handle();

        //get the ics parent listings for the certified product
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(getColumnIndexMap().getRecordStatusIndex());
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseIcsFamily(record, pendingCertifiedProduct);
            }
        }

        return pendingCertifiedProduct;
    }

    protected int parseIcsFamily(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProduct) {
        int colIndex = getColumnIndexMap().getIcsStartIndex();
        colIndex++; //ics boolean comes first; ics family second

        if (!StringUtils.isEmpty(record.get(colIndex))) {
            String icsParentUniqueId = record.get(colIndex).trim();
            PendingCertifiedProductParentListingEntity icsParentEntity = new PendingCertifiedProductParentListingEntity();
            icsParentEntity.setMappedProduct(pendingCertifiedProduct);
            icsParentEntity.setParentListingUniqueId(icsParentUniqueId);

            try {
                CertifiedProduct icsParent = cpSearchDao.getByChplProductNumber(icsParentUniqueId);
                if (icsParent != null) {
                    icsParentEntity.setParentListingId(icsParent.getId());
                }
            } catch (EntityNotFoundException ex) {
                LOGGER.info("Listing uploaded with invalid ICS source " + icsParentUniqueId);
            }
            pendingCertifiedProduct.getParentListings().add(icsParentEntity);
        }
        return 1;
    }
}
