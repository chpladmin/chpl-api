package gov.healthit.chpl.dao.search;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;

public interface CertifiedProductSearchDAO {
	
	public List<CertifiedProductFlatSearchResult> getAllCertifiedProducts();
	public CertifiedProduct getByChplProductNumber(String chplProductNumber) 
			throws EntityNotFoundException;
}
