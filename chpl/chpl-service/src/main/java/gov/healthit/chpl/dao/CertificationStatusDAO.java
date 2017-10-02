package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationStatusDTO;

public interface CertificationStatusDAO {
    List<CertificationStatusDTO> findAll();

    CertificationStatusDTO getById(Long id) throws EntityRetrievalException;

    CertificationStatusDTO getByStatusName(String statusName);
}
