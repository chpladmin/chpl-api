package gov.healthit.chpl.dao.impl;

import java.util.Date;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.PendingCertifiedProductSystemUpdateDAO;
import gov.healthit.chpl.dto.PendingCertifiedProductSystemUpdateDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductSystemUpdateEntity;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository(value = "pendingCertifiedProductSystemUpdateDAO")
public class PendingCertifiedProductSystemUpdateDAOImpl extends BaseDAOImpl implements PendingCertifiedProductSystemUpdateDAO {

	@Transactional
	public PendingCertifiedProductSystemUpdateDTO create(PendingCertifiedProductSystemUpdateDTO dto)
			throws EntityRetrievalException, EntityCreationException {
		PendingCertifiedProductSystemUpdateEntity entity = new PendingCertifiedProductSystemUpdateEntity();
		entity.setId(dto.getId());
		entity.setChangeMade(dto.getChangeMade());
		entity.setPendingCertifiedProductId(dto.getPendingCertifiedProductId());
		entity.setCreationDate(new Date());
	    entity.setDeleted(false);
	    entity.setLastModifiedDate(new Date());
	    entity.setLastModifiedUser(Util.getCurrentUser().getId());
		create(entity);
        return new PendingCertifiedProductSystemUpdateDTO(entity);
	}
	
	private void create(PendingCertifiedProductSystemUpdateEntity announcement) {

        entityManager.persist(announcement);
        entityManager.flush();
    }
}