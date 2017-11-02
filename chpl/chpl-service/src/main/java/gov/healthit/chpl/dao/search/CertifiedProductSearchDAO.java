package gov.healthit.chpl.dao.search;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.entity.search.CertifiedProductBasicSearchResultEntity;

public interface CertifiedProductSearchDAO {

    Long getListingIdByUniqueChplNumber(String chplProductNumber);
    CertifiedProduct getByChplProductNumber(String chplProductNumber) throws EntityNotFoundException;

    List<CertifiedProductFlatSearchResult> getAllCertifiedProducts();

    IcsFamilyTreeNode getICSFamilyTree(Long certifiedProductId);
    
    public List<CertifiedProductBasicSearchResultEntity> search(SearchRequest searchRequest);
}
