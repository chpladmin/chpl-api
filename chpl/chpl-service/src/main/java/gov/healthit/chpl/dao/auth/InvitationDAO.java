package gov.healthit.chpl.dao.auth;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.auth.UserInvitation;
import gov.healthit.chpl.entity.auth.InvitationEntity;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Repository(value = "invitationDAO")
@Log4j2
public class InvitationDAO extends BaseDAOImpl {
    public Long create(UserInvitation invitation) throws UserCreationException {
        InvitationEntity toCreate = new InvitationEntity();
        toCreate.setUserPermissionId(invitation.getPermission().getId());
        toCreate.setPermissionObjectId(invitation.getPermissionObjectId());
        toCreate.setEmailAddress(invitation.getEmailAddress());
        toCreate.setInviteToken(invitation.getHash());
        toCreate.setDeleted(false);
        toCreate.setLastModifiedUser(AuthUtil.getAuditId());
        super.create(toCreate);
        return toCreate.getId();
    }

    public UserInvitation update(UserInvitation invitation) throws UserRetrievalException {
        InvitationEntity toUpdate = getEntityById(invitation.getId());

        if (toUpdate == null) {
            throw new UserRetrievalException("Could not find invitation with id " + invitation.getId());
        }
        toUpdate.setConfirmToken(invitation.getConfirmationToken());
        toUpdate.setInviteToken(invitation.getInvitationToken());
        toUpdate.setCreatedUserId(invitation.getCreatedUserId());
        toUpdate.setLastModifiedDate(new Date());
        toUpdate.setLastModifiedUser(AuthUtil.getAuditId());
        super.update(toUpdate);
        return toUpdate.toDomain();
    }

    public void delete(Long id) throws UserRetrievalException {
        Date currentDate = new Date();
        InvitationEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(currentDate);
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            super.update(toDelete);
        } else {
            LOGGER.error("Unable to mark user invitation with id '" + id + "' as deleted.");
            throw new UserRetrievalException("Could not find invitation with id " + id);
        }
    }

    public UserInvitation getById(Long id) throws UserRetrievalException {
        InvitationEntity entity = getEntityById(id);
        if (entity == null) {
            return null;
        }
        return entity.toDomain();
    }

    public UserInvitation getByInvitationToken(String token) {
        InvitationEntity entity = getEntityByInvitationToken(token);
        if (entity == null) {
            return null;
        }
        return entity.toDomain();
    }

    public UserInvitation getByConfirmationToken(String token) {
        InvitationEntity entity = getEntityByConfirmToken(token);
        if (entity == null) {
            return null;
        }
        return entity.toDomain();
    }

    public UserInvitation getByCreatedUserId(Long createdUserId) {
        InvitationEntity entity = getEntityByCreatedUserId(createdUserId);
        if (entity == null) {
            return null;
        }
        return entity.toDomain();
    }

    private InvitationEntity getEntityById(Long id) throws UserRetrievalException {
        Query query = entityManager.createQuery("SELECT i "
                + "FROM InvitationEntity i "
                + "JOIN FETCH i.permission "
                + "WHERE (i.deleted = false) "
                + "AND (i.id = :id) ",
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
        Query query = entityManager.createQuery("SELECT i "
                + "FROM InvitationEntity i "
                + "JOIN FETCH i.permission "
                + "WHERE (i.deleted = false) "
                + "AND (i.inviteToken = :token) ",
                InvitationEntity.class);
        query.setParameter("token", token);
        List<InvitationEntity> result = query.getResultList();
        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private InvitationEntity getEntityByConfirmToken(String token) {
        Query query = entityManager.createQuery("SELECT i "
                + "FROM InvitationEntity i "
                + "JOIN FETCH i.permission "
                + "WHERE (i.deleted = false) "
                + "AND (i.confirmToken = :token) ",
                InvitationEntity.class);
        query.setParameter("token", token);
        List<InvitationEntity> result = query.getResultList();
        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private InvitationEntity getEntityByCreatedUserId(Long createdUserId) {
        Query query = entityManager.createQuery("SELECT i "
                + "FROM InvitationEntity i "
                + "JOIN FETCH i.permission "
                + "WHERE (i.deleted = false) "
                + "AND (i.createdUserId = :createdUserId) ",
                InvitationEntity.class);
        query.setParameter("createdUserId", createdUserId);
        List<InvitationEntity> result = query.getResultList();
        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

}
