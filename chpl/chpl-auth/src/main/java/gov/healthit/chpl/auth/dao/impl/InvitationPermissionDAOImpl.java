package gov.healthit.chpl.auth.dao.impl;


import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.InvitationPermissionDAO;
import gov.healthit.chpl.auth.dto.InvitationPermissionDTO;
import gov.healthit.chpl.auth.entity.InvitationEntity;
import gov.healthit.chpl.auth.entity.InvitationPermissionEntity;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserRetrievalException;


@Repository(value="invitationPermissionDAO")
public class InvitationPermissionDAOImpl extends BaseDAOImpl implements InvitationPermissionDAO {
	private static final Logger logger = LogManager.getLogger(InvitationPermissionDAOImpl.class);
	
	@Override
	public InvitationPermissionDTO create(InvitationPermissionDTO dto) throws UserCreationException {
		InvitationPermissionEntity permissionToCreate = new InvitationPermissionEntity();
		permissionToCreate.setCreationDate(new Date());
		permissionToCreate.setDeleted(false);
		InvitationEntity invitationEntity = new InvitationEntity();
		invitationEntity.setId(dto.getUserId());
		permissionToCreate.setInvitedUser(invitationEntity);
		permissionToCreate.setLastModifiedDate(new Date());
		permissionToCreate.setLastModifiedUser(Util.getCurrentUser().getId());
		permissionToCreate.setUserPermissionId(dto.getPermissionId());
		create(permissionToCreate);
		return new InvitationPermissionDTO(permissionToCreate);
	}
	
	@Override
	public void delete(Long id) throws UserRetrievalException {
		Date currentDate = new Date();
		
		InvitationPermissionEntity toDelete = getEntityById(id);
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(currentDate);
			//TODO: can we update the last modified user field like this? is someone authenticated at this point?
			//toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
				
			update(toDelete);
		} else {
			throw new UserRetrievalException("Could not find invited user permission with id " + id);
		}
	}
	
	private void create(InvitationPermissionEntity invitation) {
		
		entityManager.persist(invitation);
		entityManager.flush();
	}
	
	private void update(InvitationPermissionEntity invitation) {
		
		entityManager.merge(invitation);	
		entityManager.flush();
	}
	
	private InvitationPermissionEntity getEntityById(Long id) throws UserRetrievalException {
		
		InvitationPermissionEntity invitation = null;
		
		Query query = entityManager.createQuery( "from InvitationPermissionEntity where (NOT deleted = true) AND (invited_user_permission_id = :id) ", InvitationPermissionEntity.class );
		query.setParameter("id", id);
		List<InvitationPermissionEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new UserRetrievalException("Data error. Duplicate user id in database.");
		}
		
		if(result.size() == 0) {
			return null;
		}
		return result.get(0);
	}
}
