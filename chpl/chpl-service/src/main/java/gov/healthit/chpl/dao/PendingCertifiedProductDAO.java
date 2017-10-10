package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;

public interface PendingCertifiedProductDAO {

    PendingCertifiedProductDTO create(PendingCertifiedProductEntity product) throws EntityCreationException;

    void delete(Long pendingProductId) throws EntityRetrievalException;

    List<PendingCertifiedProductDTO> findAll();

    List<PendingCertifiedProductDTO> findByStatus(Long statusId);

    Long findIdByOncId(String id) throws EntityRetrievalException;

    PendingCertifiedProductDTO findById(Long pcpId, boolean includeDeleted) throws EntityRetrievalException;

    List<PendingCertifiedProductDTO> findByAcbId(Long acbId);
}
