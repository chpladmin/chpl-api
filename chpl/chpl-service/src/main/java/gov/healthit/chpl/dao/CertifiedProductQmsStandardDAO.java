package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;

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
