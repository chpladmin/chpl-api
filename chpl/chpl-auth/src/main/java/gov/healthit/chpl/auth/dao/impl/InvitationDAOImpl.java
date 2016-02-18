package gov.healthit.chpl.auth.dao.impl;


import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.InvitationDAO;
import gov.healthit.chpl.auth.dto.InvitationDTO;
import gov.healthit.chpl.auth.entity.InvitationEntity;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserRetrievalException;


@Repository(value="invitationDAO")
public class InvitationDAOImpl extends BaseDAOImpl implements InvitationDAO {
	private static final Logger logger = LogManager.getLogger(InvitationDAOImpl.class);
	
	@Override
	public InvitationDTO create(InvitationDTO dto) throws UserCreationException {
		Date creationDate = new Date();
		
		InvitationEntity toCreate = new InvitationEntity();
		toCreate.setCreationDate(creationDate);
		toCreate.setDeleted(false);
		toCreate.setAcbId(dto.getAcbId());
		toCreate.setTestingLabId(dto.getTestingLabId());
		toCreate.setEmailAddress(dto.getEmail());
		toCreate.setInviteToken(dto.getInviteToken());
		toCreate.setLastModifiedDate(new Date());
		toCreate.setLastModifiedUser(Util.getCurrentUser().getId());
		
		create(toCreate);
		return new InvitationDTO(toCreate);
	}
	
	@Override
	public InvitationDTO update(InvitationDTO dto) throws UserRetrievalException {
		InvitationEntity toUpdate = getEntityById(dto.getId());
		
		if(toUpdate == null) {
			throw new UserRetrievalException("Could not find invitation with id " + dto.getId());
		}
		toUpdate.setConfirmToken(dto.getConfirmToken());
		toUpdate.setInviteToken(dto.getInviteToken());
		toUpdate.setCreatedUserId(dto.getCreatedUserId());
		toUpdate.setLastModifiedDate(new Date());
		toUpdate.setLastModifiedUser(Util.getCurrentUser().getId());
		
		update(toUpdate);
		
		return new InvitationDTO(toUpdate);
	}
	
	@Override
	public void delete(Long id) throws UserRetrievalException {
		Date currentDate = new Date();
		
		InvitationEntity toDelete = getEntityById(id);
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(currentDate);
			//TODO: can we update the last modified user field like this? is someone authenticated at this point?
			//toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			
			update(toDelete);
		} else {
			throw new UserRetrievalException("Could not find invitation with id " + id);
		}
	}
	
	@Override
	public InvitationDTO getById(Long id) throws UserRetrievalException {
		InvitationEntity entity = getEntityById(id);
		if(entity == null) {
			return null;
		}
		return new InvitationDTO(entity);
	}
	
	@Override
	public InvitationDTO getByInvitationToken(String token) {
		InvitationEntity entity = getEntityByInvitationToken(token);
		if(entity == null) {
			return null;
		}
		return new InvitationDTO(entity);
	}
	
	@Override
	public InvitationDTO getByConfirmationToken(String token) {
		InvitationEntity entity = getEntityByConfirmToken(token);
		if(entity == null) {
			return null;
		}
		return new InvitationDTO(entity);
	}
	
	private void create(InvitationEntity invitation) {
		
		entityManager.persist(invitation);
		entityManager.flush();
	}
	
	private void update(InvitationEntity invitation) {
		
		entityManager.merge(invitation);	
		entityManager.flush();
	}
	
	private InvitationEntity getEntityById(Long id) throws UserRetrievalException {
		
		InvitationEntity invitation = null;
		
		Query query = entityManager.createQuery( "from InvitationEntity where (NOT deleted = true) AND (invited_user_id = :id) ", InvitationEntity.class );
		query.setParameter("id", id);
		List<InvitationEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new UserRetrievalException("Data error. Duplicate user id in database.");
		}
		
		if(result.size() == 0) {
			return null;
		}
		return result.get(0);
	}
	
	private InvitationEntity getEntityByInvitationToken(String token) {
		
		InvitationEntity invitation = null;
		
		Query query = entityManager.createQuery( "from InvitationEntity where (NOT deleted = true) AND (invite_token = :token) ", InvitationEntity.class );
		query.setParameter("token", token);
		List<InvitationEntity> result = query.getResultList();

		if(result.size() == 0) {
			return null;
		}
		return result.get(0);
	}
	
	private InvitationEntity getEntityByConfirmToken(String token) {
		
		InvitationEntity invitation = null;
		
		Query query = entityManager.createQuery( "from InvitationEntity where (NOT deleted = true) AND (confirm_token = :token) ", InvitationEntity.class );
		query.setParameter("token", token);
		List<InvitationEntity> result = query.getResultList();

		if(result.size() == 0) {
			return null;
		}
		return result.get(0);
	}
}
