package gov.healthit.chpl.upload.certifiedProduct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap2015Version6;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Adds old B3 back in
 *
 */
@Component("certifiedProductHandler2015Version6")
public class CertifiedProductHandler2015Version6 extends CertifiedProductHandler2015Version5 {
    private TemplateColumnIndexMap templateColumnIndexMap;

    @Autowired
    public CertifiedProductHandler2015Version6(final ErrorMessageUtil msgUtil) {
        super(msgUtil);
        templateColumnIndexMap = new TemplateColumnIndexMap2015Version6();
    }

    @Override
    public TemplateColumnIndexMap getColumnIndexMap() {
        return templateColumnIndexMap;
    }
}
