package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CertificationEditionDAO {

    void create(CertificationEditionDTO dto) throws EntityCreationException, EntityRetrievalException;

    void update(CertificationEditionDTO dto) throws EntityRetrievalException;

    void delete(Long id);

    List<CertificationEditionDTO> findAll();

    List<CertificationEditionDTO> getEditions(List<Long> listingIds);

    CertificationEditionDTO getById(Long id) throws EntityRetrievalException;

    CertificationEditionDTO getByYear(String year);

}
