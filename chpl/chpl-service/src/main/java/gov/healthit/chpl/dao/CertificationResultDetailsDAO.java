package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Interface for databaes access to certification result details view.
 * @author kekey
 *
 */
public interface CertificationResultDetailsDAO {

    List<CertificationResultDetailsDTO> getCertificationResultDetailsByCertifiedProductId(
            Long certifiedProductId) throws EntityRetrievalException;

    List<CertificationResultDetailsDTO> getCertificationResultDetailsByCertifiedProductIdSED(
            Long certifiedProductId) throws EntityRetrievalException;

}
