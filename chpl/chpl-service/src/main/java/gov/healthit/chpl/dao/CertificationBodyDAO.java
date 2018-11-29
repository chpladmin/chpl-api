package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CertificationBodyDAO {

    public CertificationBodyDTO create(CertificationBodyDTO acb)
            throws EntityRetrievalException, EntityCreationException;
    public List<CertificationBodyDTO> findAll();
    public List<CertificationBodyDTO> findAllActive();
    public CertificationBodyDTO getById(Long id) throws EntityRetrievalException;
    public CertificationBodyDTO getByName(String name);
    public String getMaxCode();
    public CertificationBodyDTO update(CertificationBodyDTO contact) throws EntityRetrievalException;
}
