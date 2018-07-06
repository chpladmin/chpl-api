package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CertifiedProductQmsStandardDAO {

    List<CertifiedProductQmsStandardDTO> getQmsStandardsByCertifiedProductId(Long certifiedProductId)
            throws EntityRetrievalException;

    CertifiedProductQmsStandardDTO lookupMapping(Long certifiedProductId, Long qmsStandardId)
            throws EntityRetrievalException;

    CertifiedProductQmsStandardDTO updateCertifiedProductQms(CertifiedProductQmsStandardDTO toUpdate)
            throws EntityRetrievalException;

    CertifiedProductQmsStandardDTO createCertifiedProductQms(CertifiedProductQmsStandardDTO toCreate)
            throws EntityCreationException;

    CertifiedProductQmsStandardDTO deleteCertifiedProductQms(Long id) throws EntityRetrievalException;

}
