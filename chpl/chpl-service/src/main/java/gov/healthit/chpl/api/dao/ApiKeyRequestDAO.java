package gov.healthit.chpl.api.dao;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.api.domain.ApiKeyRegistration;
import gov.healthit.chpl.api.domain.ApiKeyRequest;
import gov.healthit.chpl.api.entity.ApiKeyRequestEntity;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Repository
public class ApiKeyRequestDAO extends BaseDAOImpl {
    private ErrorMessageUtil errorMessages;

    @Autowired
    public ApiKeyRequestDAO(ErrorMessageUtil errorMessages) {
        this.errorMessages = errorMessages;
    }

    public ApiKeyRequest create(ApiKeyRegistration apiKeyRegistration) {
        return new ApiKeyRequest(create(ApiKeyRequestEntity.builder()
                .email(apiKeyRegistration.getEmail())
                .nameOrganization(apiKeyRegistration.getName())
                .apiRequestToken(Util.md5(apiKeyRegistration.getEmail() + toEpochMilli(LocalDateTime.now())))
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .lastModifiedUser(User.SYSTEM_USER_ID)
                .deleted(false)
                .build()));
    }

    public void delete(Long id) throws EntityRetrievalException {
        ApiKeyRequestEntity entity = getEntityById(id);
        entity.setDeleted(true);
        update(entity);
    }

    public Optional<ApiKeyRequest> getByEmail(String email) {
        Optional<ApiKeyRequestEntity> entity = getEntityByEmail(email);
        if (entity.isPresent()) {
            return Optional.of(new ApiKeyRequest(entity.get()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<ApiKeyRequest> getByApiRequestToken(String apiRequestToken) {
        Optional<ApiKeyRequestEntity> entity = getEntityByApiRequestToken(apiRequestToken);
        if (entity.isPresent()) {
            return Optional.of(new ApiKeyRequest(entity.get()));
        } else {
            return Optional.empty();
        }
    }


    private ApiKeyRequestEntity create(ApiKeyRequestEntity entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    private ApiKeyRequestEntity update(ApiKeyRequestEntity entity) {
        entityManager.merge(entity);
        entityManager.flush();
        return entity;
    }

    private ApiKeyRequestEntity getEntityById(Long id) throws EntityRetrievalException {
        List<ApiKeyRequestEntity> result = entityManager.createQuery(
                "from ApiKeyRequestEntity "
                + "where (id = :id) "
                + "and deleted <> true",
                ApiKeyRequestEntity.class)
                .setParameter("id", id)
                .getResultList();

        if (result.size() == 0) {
            throw new EntityRetrievalException(errorMessages.getMessage("apiKeyRequest.notFound"));
        } else {
            return result.get(0);
        }
    }

    private Optional<ApiKeyRequestEntity> getEntityByEmail(String email) {
        List<ApiKeyRequestEntity> result = entityManager.createQuery(
                "from ApiKeyRequestEntity "
                + "where (email = :email) "
                + "and deleted <> true",
                ApiKeyRequestEntity.class)
                .setParameter("email", email)
                .getResultList();

        if (result.size() > 0) {
            return Optional.of(result.get(0));
        } else {
            return Optional.empty();
        }
    }

    private Optional<ApiKeyRequestEntity> getEntityByApiRequestToken(String apiRequestToken) {
        List<ApiKeyRequestEntity> result = entityManager.createQuery(
                "from ApiKeyRequestEntity "
                + "where (apiRequestToken = :apiRequestToken) "
                + "and deleted <> true",
                ApiKeyRequestEntity.class)
                .setParameter("apiRequestToken", apiRequestToken)
                .getResultList();

        if (result.size() > 0) {
            return Optional.of(result.get(0));
        } else {
            return Optional.empty();
        }
    }

    private long toEpochMilli(LocalDateTime localDateTime) {
      return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
