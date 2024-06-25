package gov.healthit.chpl.user.cognito;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class CognitoUserInvitationDAO extends BaseDAOImpl {

    public CognitoUserInvitation create(CognitoUserInvitation userInvitation) {
        CognitoUserInvitationEntity entity = CognitoUserInvitationEntity.builder()
                .email(userInvitation.getEmail())
                .token(userInvitation.getInvitationToken())
                .groupName(userInvitation.getGroupName())
                .organizationId(userInvitation.getOrganizationId() != null ? userInvitation.getOrganizationId() : null)
                .build();

        create(entity);

        return getEntityById(entity.getId()).toDomain();
    }

    public void deleteByToken(UUID token) {
        CognitoUserInvitationEntity entity = getEntityByToken(token);
        if (entity != null) {
            entity.setDeleted(true);
            update(entity);
        }

    }

    public CognitoUserInvitation getByToken(UUID token) {
        return getEntityByToken(token).toDomain();
    }

    public CognitoUserInvitation getById(Long id) {
        return getEntityById(id).toDomain();
    }


    private CognitoUserInvitationEntity getEntityByToken(UUID token) {
        CognitoUserInvitationEntity entity = null;

        Query query = entityManager
                .createQuery("SELECT ui "
                        + "FROM CognitoUserInvitationEntity ui "
                        + "WHERE (NOT ui.deleted = true) "
                        + "AND (ui.token = :token) ",
                        CognitoUserInvitationEntity.class);
        query.setParameter("token", token);
        List<CognitoUserInvitationEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private CognitoUserInvitationEntity getEntityById(Long id) {
        CognitoUserInvitationEntity entity = null;

        Query query = entityManager
                .createQuery("SELECT ui "
                        + "FROM CognitoUserInvitationEntity ui "
                        + "WHERE (NOT ui.deleted = true) "
                        + "AND (ui.id = :id) ",
                        CognitoUserInvitationEntity.class);
        query.setParameter("id", id);
        List<CognitoUserInvitationEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

}
