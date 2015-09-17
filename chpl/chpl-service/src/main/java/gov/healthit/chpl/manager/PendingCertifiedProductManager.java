package gov.healthit.chpl.manager;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;


public interface PendingCertifiedProductManager {
	public List<PendingCertifiedProductDetails> upload(CertificationBodyDTO acb, MultipartFile file) throws InvalidArgumentsException, IOException;
	public void delete(PendingCertifiedProductDTO product);
	public List<PendingCertifiedProductDTO> getAll();
	public PendingCertifiedProductDTO getById(Long id) throws EntityRetrievalException;
	public List<PendingCertifiedProductDTO> getByAcb(CertificationBodyDTO acb);
}
