package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CQMResultDetailsDAO {

    List<CQMResultDetailsDTO> getCQMResultDetailsByCertifiedProductId(Long certifiedProductId)
            throws EntityRetrievalException;

}
