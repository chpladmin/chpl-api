package gov.healthit.chpl.api.deprecatedUsage;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import lombok.extern.log4j.Log4j2;

@Repository("deprecatedApiUsageDao")
@Log4j2
public class DeprecatedApiUsageDao extends BaseDAOImpl {

    @Transactional
    public DeprecatedApi getDeprecatedApi(HttpMethod httpMethod, String apiOperation, String requestParameter) {
        String hql = "SELECT api "
                + "FROM DeprecatedApiEntity api "
                + "WHERE deleted = false "
                + "AND httpMethod = :httpMethod "
                + "AND apiOperation = :apiOperation";
        if (!StringUtils.isEmpty(requestParameter)) {
            hql += " AND requestParameter = :requestParameter";
        }
        Query query = entityManager.createQuery(hql);
        query.setParameter("httpMethod", httpMethod.name());
        query.setParameter("apiOperation", apiOperation);
        if (!StringUtils.isEmpty(requestParameter)) {
            query.setParameter("requestParameter", requestParameter);
        }
        List<DeprecatedApiEntity> matchingDeprecatedApis = query.getResultList();
        if (matchingDeprecatedApis == null || matchingDeprecatedApis.size() == 0) {
            LOGGER.error("Deprecated API method = '" + httpMethod.name() + "', apiOperation = '" + apiOperation + "', requestParamter = '" + requestParameter + "' was not found.");
            return null;
        }
        return matchingDeprecatedApis.get(0).toDomain();
    }

    @Transactional
    public List<DeprecatedApiUsage> getAllDeprecatedApiUsage() {
        String hql = "SELECT apiUsage "
                + "FROM DeprecatedApiUsageEntity apiUsage "
                + "JOIN FETCH apiUsage.deprecatedApi api "
                + "JOIN FETCH apiUsage.apiKey apiKey "
                + "WHERE apiUsage.deleted = false "
                + "ORDER BY apiUsage.apiCallCount DESC ";
        Query query = entityManager.createQuery(hql);
        List<DeprecatedApiUsageEntity> results = query.getResultList();
        return results.stream().map(result -> result.toDomain()).collect(Collectors.toList());
    }

    @Transactional
    public void createOrUpdateDeprecatedApiUsage(DeprecatedApiUsage apiUsage) {
        DeprecatedApiUsageEntity existingEntity = getDeprecatedApiUsage(apiUsage.getApiKey().getId(), apiUsage.getApi().getId());
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
        DeprecatedApiUsageEntity entity = entityManager.find(DeprecatedApiUsageEntity.class, id);
        if (entity != null) {
            entity.setDeleted(true);
            entity.setLastModifiedUser(User.SYSTEM_USER_ID);
            update(entity);
        }
    }

    private void create(DeprecatedApiUsage apiUsage) {
        DeprecatedApiUsageEntity entity = new DeprecatedApiUsageEntity();
        entity.setApiCallCount(1L);
        entity.setApiKeyId(apiUsage.getApiKey().getId());
        entity.setDeleted(false);
        entity.setDeprecatedApiId(apiUsage.getApi().getId());
        entity.setLastAccessedDate(new Date());
        entity.setLastModifiedUser(User.SYSTEM_USER_ID);
        create(entity);
    }

    private DeprecatedApiUsageEntity getDeprecatedApiUsage(Long apiKeyId, Long deprecatedApiId) {
        String hql = "SELECT apiUsage "
                + "FROM DeprecatedApiUsageEntity apiUsage "
                + "JOIN FETCH apiUsage.deprecatedApi api "
                + "JOIN FETCH apiUsage.apiKey apiKey "
                + "WHERE apiUsage.deleted = false "
                + "AND apiUsage.apiKeyId = :apiKeyId "
                + "AND apiUsage.deprecatedApiId = :deprecatedApiId";
        Query query = entityManager.createQuery(hql);
        query.setParameter("apiKeyId", apiKeyId);
        query.setParameter("deprecatedApiId", deprecatedApiId);
        List<DeprecatedApiUsageEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            LOGGER.info("No deprecated api usage exists for api key ID " + apiKeyId + " and deprecated api ID: " + deprecatedApiId);
            return null;
        }
        return results.get(0);
    }
}
