package gov.healthit.chpl.dao.search;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;

public interface CertifiedProductSearchDAO {

    Long getListingIdByUniqueChplNumber(String chplProductNumber);

    List<CertifiedProductFlatSearchResult> getAllCertifiedProducts();

    CertifiedProduct getByChplProductNumber(String chplProductNumber) throws EntityNotFoundException;

    IcsFamilyTreeNode getICSFamilyTree(Long certifiedProductId);
}
