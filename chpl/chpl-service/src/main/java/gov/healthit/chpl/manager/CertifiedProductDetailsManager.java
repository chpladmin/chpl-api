package gov.healthit.chpl.manager;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;

public interface CertifiedProductDetailsManager {
    CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId)
            throws EntityRetrievalException;
}
