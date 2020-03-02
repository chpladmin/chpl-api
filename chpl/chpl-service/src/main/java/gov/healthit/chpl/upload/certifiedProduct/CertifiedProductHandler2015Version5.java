package gov.healthit.chpl.upload.certifiedProduct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap2015Version5;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Removed 'removed' criteria, includes all Cures criteria, adds Self Developer
 *
 */
@Component("certifiedProductHandler2015Version5")
public class CertifiedProductHandler2015Version5 extends CertifiedProductHandler2015Version4 {
    private TemplateColumnIndexMap templateColumnIndexMap;

    @Autowired
    public CertifiedProductHandler2015Version5(final ErrorMessageUtil msgUtil) {
        super(msgUtil);
        templateColumnIndexMap = new TemplateColumnIndexMap2015Version5();
    }

    @Override
    public TemplateColumnIndexMap getColumnIndexMap() {
        return templateColumnIndexMap;
    }
}
