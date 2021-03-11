package gov.healthit.chpl.api.dao;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.api.domain.ApiKeyRegistration;
import gov.healthit.chpl.api.domain.ApiKeyRequest;
import gov.healthit.chpl.api.entity.ApiKeyRequestEntity;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.util.Util;

@Repository
public class ApiKeyRequestDAO extends BaseDAOImpl {
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

    public Optional<ApiKeyRequest> getByEmail(String email) {
        Optional<ApiKeyRequestEntity> entity = getEntityByEmail(email);
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

    private long toEpochMilli(LocalDateTime localDateTime) {
      return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
