package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.MeaningfulUseUserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Interface for database access to meaningful use user counts.
 * @author kekey
 *
 */
public interface MeaningfulUseUserDAO {

    MeaningfulUseUserDTO create(MeaningfulUseUserDTO dto)
            throws EntityCreationException, EntityRetrievalException;

    MeaningfulUseUserDTO update(MeaningfulUseUserDTO dto) throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    MeaningfulUseUserDTO getById(Long id) throws EntityRetrievalException;

    List<MeaningfulUseUserDTO> findByCertifiedProductId(Long certifiedProductId);

}
