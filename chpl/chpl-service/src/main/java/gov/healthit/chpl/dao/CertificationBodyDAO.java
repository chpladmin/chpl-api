package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CertificationBodyDAO {

    CertificationBodyDTO create(CertificationBodyDTO acb)
            throws EntityRetrievalException, EntityCreationException;

    List<CertificationBodyDTO> findAll();

    CertificationBodyDTO getById(Long id) throws EntityRetrievalException;

    CertificationBodyDTO getByName(String name);

    String getMaxCode();

    CertificationBodyDTO update(CertificationBodyDTO contact) throws EntityRetrievalException;

}
