package gov.healthit.chpl.upload.certifiedProduct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap2015Version4;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Adds Cures revised criteria b3 and two new Cures criteria d12 and d13.
 *
 */
@Component("certifiedProductHandler2015Version4")
public class CertifiedProductHandler2015Version4 extends CertifiedProductHandler2015Version3 {
    private TemplateColumnIndexMap templateColumnIndexMap;

    @Autowired
    public CertifiedProductHandler2015Version4(final ErrorMessageUtil msgUtil) {
        super(msgUtil);
        templateColumnIndexMap = new TemplateColumnIndexMap2015Version4();
    }

    @Override
    public TemplateColumnIndexMap getColumnIndexMap() {
        return templateColumnIndexMap;
    }
}
