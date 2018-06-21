package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationStatusEventDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CertificationStatusEventDAO {

    CertificationStatusEventDTO create(CertificationStatusEventDTO dto)
            throws EntityCreationException, EntityRetrievalException;

    CertificationStatusEventDTO update(CertificationStatusEventDTO dto) throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    CertificationStatusEventDTO getById(Long id) throws EntityRetrievalException;

    List<CertificationStatusEventDTO> findAll();

    List<CertificationStatusEventDTO> findByCertifiedProductId(Long certifiedProductId);

    CertificationStatusEventDTO findInitialCertificationEventForCertifiedProduct(Long certifiedProductId);

}
