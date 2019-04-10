package gov.healthit.chpl.dao.auth.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.auth.InvitationDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.auth.InvitationDTO;
import gov.healthit.chpl.entity.auth.InvitationEntity;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository(value = "invitationDAO")
public class InvitationDAOImpl extends BaseDAOImpl implements InvitationDAO {
    private static final Logger LOGGER = LogManager.getLogger(InvitationDAOImpl.class);

    @Override
    public InvitationDTO create(final InvitationDTO dto) throws UserCreationException {
        Date creationDate = new Date();

        InvitationEntity toCreate = new InvitationEntity();
        toCreate.setCreationDate(creationDate);
        toCreate.setDeleted(false);
        toCreate.setUserPermissionId(dto.getPermission().getId());
        toCreate.setPermissionObjectId(dto.getPermissionObjectId());
        toCreate.setEmailAddress(dto.getEmail());
        toCreate.setInviteToken(dto.getInviteToken());
        toCreate.setLastModifiedDate(new Date());
        toCreate.setLastModifiedUser(AuthUtil.getAuditId());
        super.create(toCreate);
        return new InvitationDTO(toCreate);
    }

    @Override
    public InvitationDTO update(final InvitationDTO dto) throws UserRetrievalException {
        InvitationEntity toUpdate = getEntityById(dto.getId());

        if (toUpdate == null) {
            throw new UserRetrievalException("Could not find invitation with id " + dto.getId());
        }
        toUpdate.setConfirmToken(dto.getConfirmToken());
        toUpdate.setInviteToken(dto.getInviteToken());
        toUpdate.setCreatedUserId(dto.getCreatedUserId());
        toUpdate.setLastModifiedDate(new Date());
        toUpdate.setLastModifiedUser(AuthUtil.getAuditId());
        super.update(toUpdate);
        return new InvitationDTO(toUpdate);
    }

    @Override
    public void delete(final Long id) throws UserRetrievalException {
        Date currentDate = new Date();
        InvitationEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(currentDate);
            // TODO: can we update the last modified user field like this? is
            // someone authenticated at this point?
            // toDelete.setLastModifiedUser(Util.getAuditId());
            super.update(toDelete);
        } else {
            throw new UserRetrievalException("Could not find invitation with id " + id);
        }
    }

    @Override
    public InvitationDTO getById(final Long id) throws UserRetrievalException {
        InvitationEntity entity = getEntityById(id);
        if (entity == null) {
            return null;
        }
        return new InvitationDTO(entity);
    }

    @Override
    public InvitationDTO getByInvitationToken(final String token) {
        InvitationEntity entity = getEntityByInvitationToken(token);
        if (entity == null) {
            return null;
        }
        return new InvitationDTO(entity);
    }

    @Override
    public InvitationDTO getByConfirmationToken(final String token) {
        InvitationEntity entity = getEntityByConfirmToken(token);
        if (entity == null) {
            return null;
        }
        return new InvitationDTO(entity);
    }

    private InvitationEntity getEntityById(final Long id) throws UserRetrievalException {
        Query query = entityManager.createQuery(
                "from InvitationEntity where (NOT deleted = true) AND (id = :id) ",
                InvitationEntity.class);
        query.setParameter("id", id);
        List<InvitationEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new UserRetrievalException("Data error. Duplicate invitation id in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private InvitationEntity getEntityByInvitationToken(String token) {
        Query query = entityManager.createQuery(
                "from InvitationEntity where (NOT deleted = true) AND (inviteToken = :token) ",
                InvitationEntity.class);
        query.setParameter("token", token);
        List<InvitationEntity> result = query.getResultList();

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private InvitationEntity getEntityByConfirmToken(final String token) {
        Query query = entityManager.createQuery(
                "from InvitationEntity where (NOT deleted = true) AND (confirmToken = :token) ",
                InvitationEntity.class);
        query.setParameter("token", token);
        List<InvitationEntity> result = query.getResultList();

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }
}
