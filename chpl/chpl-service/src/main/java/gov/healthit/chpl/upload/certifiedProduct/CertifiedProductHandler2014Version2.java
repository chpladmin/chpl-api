package gov.healthit.chpl.upload.certifiedProduct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap2014Version2;

/**
 * Add GAP column 170.314 (b)(5)(B).
 * @author blindsey
 *
 */
@Component("certifiedProductHandler2014Version2")
public class CertifiedProductHandler2014Version2 extends CertifiedProductHandler2014Version1 {

    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductHandler2014Version2.class);
    private TemplateColumnIndexMap templateColumnIndexMap;

    public CertifiedProductHandler2014Version2() {
        templateColumnIndexMap = new TemplateColumnIndexMap2014Version2();
    }

    @Override
    public TemplateColumnIndexMap getColumnIndexMap() {
        return templateColumnIndexMap;
    }

    public PendingCertifiedProductEntity handle() {
        PendingCertifiedProductEntity pendingCertifiedProduct = super.handle();
        return pendingCertifiedProduct;
    }
}
