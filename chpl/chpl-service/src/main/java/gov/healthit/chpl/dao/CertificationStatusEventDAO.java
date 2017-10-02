package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationStatusEventDTO;

public interface CertificationStatusEventDAO {

    public CertificationStatusEventDTO create(CertificationStatusEventDTO dto)
            throws EntityCreationException, EntityRetrievalException;

    public CertificationStatusEventDTO update(CertificationStatusEventDTO dto) throws EntityRetrievalException;

    public void delete(Long id) throws EntityRetrievalException;

    public CertificationStatusEventDTO getById(Long id) throws EntityRetrievalException;

    public List<CertificationStatusEventDTO> findAll();

    public List<CertificationStatusEventDTO> findByCertifiedProductId(Long certifiedProductId);

    public CertificationStatusEventDTO findInitialCertificationEventForCertifiedProduct(Long certifiedProductId);

}
