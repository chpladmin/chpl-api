package gov.healthit.chpl.user.cognito.password;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class CognitoForgotPasswordDAO extends BaseDAOImpl {

    public CognitoForgotPassword create(CognitoForgotPassword forgotPassword) {
        CognitoForgotPasswordEntity entity = CognitoForgotPasswordEntity.builder()
                .email(forgotPassword.getEmail())
                .token(forgotPassword.getToken())
                .build();

        create(entity);

        return getEntityById(entity.getId()).toDomain();
    }

    public void deleteByToken(UUID token) {
        CognitoForgotPasswordEntity entity = getEntityByToken(token);
        if (entity != null) {
            entity.setDeleted(true);
            update(entity);
        }

    }

    public CognitoForgotPassword getByToken(UUID token) {
        return getEntityByToken(token).toDomain();
    }

    public CognitoForgotPassword getById(Long id) {
        return getEntityById(id).toDomain();
    }


    private CognitoForgotPasswordEntity getEntityByToken(UUID token) {
        CognitoForgotPasswordEntity entity = null;

        Query query = entityManager
                .createQuery("SELECT fp "
                        + "FROM CognitoForgotPasswordEntity fp "
                        + "WHERE (NOT fp.deleted = true) "
                        + "AND (fp.token = :token) ",
                        CognitoForgotPasswordEntity.class);
        query.setParameter("token", token);
        List<CognitoForgotPasswordEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private CognitoForgotPasswordEntity getEntityById(Long id) {
        CognitoForgotPasswordEntity entity = null;

        Query query = entityManager
                .createQuery("SELECT fp "
                        + "FROM CognitoForgotPasswordEntity fp "
                        + "WHERE (NOT fp.deleted = true) "
                        + "AND (fp.id = :id) ",
                        CognitoForgotPasswordEntity.class);
        query.setParameter("id", id);
        List<CognitoForgotPasswordEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

}
