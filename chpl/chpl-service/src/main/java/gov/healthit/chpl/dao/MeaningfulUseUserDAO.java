package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.MeaningfulUseUserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Interface for MUU dao.
 * @author kekey
 *
 */
public interface MeaningfulUseUserDAO {

    MeaningfulUseUserDTO create(final MeaningfulUseUserDTO dto)
            throws EntityCreationException, EntityRetrievalException;

    MeaningfulUseUserDTO update(final MeaningfulUseUserDTO dto) throws EntityRetrievalException;

    void delete(final Long id) throws EntityRetrievalException;

    MeaningfulUseUserDTO getById(final Long id) throws EntityRetrievalException;

    List<MeaningfulUseUserDTO> findByCertifiedProductId(final Long certifiedProductId);

}
