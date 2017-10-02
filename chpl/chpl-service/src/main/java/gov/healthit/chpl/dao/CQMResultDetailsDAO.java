package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CQMResultDetailsDTO;

public interface CQMResultDetailsDAO {

    public List<CQMResultDetailsDTO> getCQMResultDetailsByCertifiedProductId(Long certifiedProductId)
            throws EntityRetrievalException;

}
