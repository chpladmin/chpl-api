package gov.healthit.chpl.upload.certifiedProduct;

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap2015Version2;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

/**
 * Add GAP column 170.314 (b)(5)(B)
 * @author blindsey
 *
 */
@Component("certifiedProductHandler2014Version2")
public class CertifiedProductHandler2014Version2 extends CertifiedProductHandler2014Version1 {

    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductHandler2014Version2.class);
    private TemplateColumnIndexMap templateColumnIndexMap;
    
    public PendingCertifiedProductEntity handle() {
        PendingCertifiedProductEntity pendingCertifiedProduct = super.handle();
        return pendingCertifiedProduct;
    }
    
    public CertifiedProductHandler2014Version2() {
        templateColumnIndexMap = new TemplateColumnIndexMap2015Version2();
    }
    
    @Override
    public TemplateColumnIndexMap getColumnIndexMap() {
        return templateColumnIndexMap;
    }
}
