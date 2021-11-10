package gov.healthit.chpl.api.deprecatedUsage;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import lombok.extern.log4j.Log4j2;

@Repository("deprecatedResponseFieldApiUsageDao")
@Log4j2
public class DeprecatedResponseFieldApiUsageDao extends BaseDAOImpl {

    @Transactional
    public DeprecatedResponseFieldApi getDeprecatedApi(HttpMethod httpMethod, String apiOperation) {
        String hql = "SELECT api "
                + "FROM DeprecatedResponseFieldApiEntity api "
                + "JOIN FETCH api.responseFields rfs "
                + "WHERE api.deleted = false "
                + "AND api.httpMethod = :httpMethod "
                + "AND api.apiOperation = :apiOperation";

        Query query = entityManager.createQuery(hql);
        query.setParameter("httpMethod", httpMethod.name());
        query.setParameter("apiOperation", apiOperation);

        List<DeprecatedResponseFieldApiEntity> matchingDeprecatedApis = query.getResultList();
        if (matchingDeprecatedApis == null || matchingDeprecatedApis.size() == 0) {
            LOGGER.error("Deprecated API method = '" + httpMethod.name() + "', apiOperation = '" + apiOperation + "' was not found.");
            return null;
        }
        return matchingDeprecatedApis.get(0).toDomain();
    }

    @Transactional
    public List<DeprecatedResponseFieldApiUsage> getAllUsage() {
        String hql = "SELECT DISTINCT apiUsage "
                + "FROM DeprecatedResponseFieldApiUsageEntity apiUsage "
                + "JOIN FETCH apiUsage.deprecatedResponseFieldApi api "
                + "JOIN FETCH api.responseFields rf "
                + "JOIN FETCH apiUsage.apiKey apiKey "
                + "WHERE apiUsage.deleted = false "
                + "ORDER BY apiUsage.apiCallCount DESC ";
        Query query = entityManager.createQuery(hql);
        List<DeprecatedResponseFieldApiUsageEntity> results = query.getResultList();
        return results.stream().map(result -> result.toDomain()).collect(Collectors.toList());
    }

    @Transactional
    public void createOrUpdateUsage(DeprecatedResponseFieldApiUsage apiUsage) {
        DeprecatedResponseFieldApiUsageEntity existingEntity = getUsage(apiUsage.getApiKey().getId(), apiUsage.getApi().getId());
        if (existingEntity == null) {
            create(apiUsage);
        } else {
            existingEntity.setApiCallCount(existingEntity.getApiCallCount() + 1);
            existingEntity.setLastAccessedDate(new Date());
            update(existingEntity);
        }
    }

    @Transactional
    public void delete(Long id) {
        DeprecatedResponseFieldApiUsageEntity entity = entityManager.find(DeprecatedResponseFieldApiUsageEntity.class, id);
        if (entity != null) {
            entity.setDeleted(true);
            entity.setLastModifiedUser(User.SYSTEM_USER_ID);
            update(entity);
        }
    }

    private void create(DeprecatedResponseFieldApiUsage apiUsage) {
        DeprecatedResponseFieldApiUsageEntity entity = new DeprecatedResponseFieldApiUsageEntity();
        entity.setApiCallCount(1L);
        entity.setApiKeyId(apiUsage.getApiKey().getId());
        entity.setDeleted(false);
        entity.setDeprecatedResponseFieldApiId(apiUsage.getApi().getId());
        entity.setLastAccessedDate(new Date());
        entity.setLastModifiedUser(User.SYSTEM_USER_ID);
        create(entity);
    }

    private DeprecatedResponseFieldApiUsageEntity getUsage(Long apiKeyId, Long deprecatedResponseFieldApiId) {
        String hql = "SELECT apiUsage "
                + "FROM DeprecatedResponseFieldApiUsageEntity apiUsage "
                + "JOIN FETCH apiUsage.deprecatedResponseFieldApi api "
                + "JOIN FETCH api.responseFields rf "
                + "JOIN FETCH apiUsage.apiKey apiKey "
                + "WHERE apiUsage.deleted = false "
                + "AND apiUsage.apiKeyId = :apiKeyId "
                + "AND apiUsage.deprecatedResponseFieldApiId = :deprecatedResponseFieldApiId";
        Query query = entityManager.createQuery(hql);
        query.setParameter("apiKeyId", apiKeyId);
        query.setParameter("deprecatedResponseFieldApiId", deprecatedResponseFieldApiId);
        List<DeprecatedResponseFieldApiUsageEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            LOGGER.info("No deprecated api usage exists for api key ID " + apiKeyId + " and deprecated response field api ID: " + deprecatedResponseFieldApiId);
            return null;
        }
        return results.get(0);
    }
}
