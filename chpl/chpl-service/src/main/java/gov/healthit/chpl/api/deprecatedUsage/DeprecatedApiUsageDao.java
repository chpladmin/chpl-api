package gov.healthit.chpl.api.deprecatedUsage;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import lombok.extern.log4j.Log4j2;

@Repository("deprecatedApiUsageDao")
@Log4j2
public class DeprecatedApiUsageDao extends BaseDAOImpl {

    @Transactional
    public List<DeprecatedApiUsage> getUnnotifiedUsage() {
        String hql = "SELECT apiUsage "
                + "FROM DeprecatedApiUsageEntity apiUsage "
                + "JOIN FETCH apiUsage.apiKey apiKey "
                + "WHERE apiUsage.notificationSent IS NULL "
                + "AND apiUsage.deleted = false "
                + "ORDER BY apiUsage.apiCallCount DESC ";
        Query query = entityManager.createQuery(hql);
        List<DeprecatedApiUsageEntity> results = query.getResultList();
        return results.stream().map(result -> result.toDomain()).collect(Collectors.toList());
    }

    @Transactional
    public void createOrUpdateDeprecatedApiUsage(DeprecatedApiUsage apiUsage) {
        DeprecatedApiUsageEntity existingEntity = getDeprecatedApiUsage(apiUsage.getApiKey().getId(),
                apiUsage.getHttpMethod(), apiUsage.getApiOperation(),
                apiUsage.getResponseField());
        if (existingEntity == null) {
            create(apiUsage);
        } else {
            existingEntity.setApiCallCount(existingEntity.getApiCallCount() + 1);
            existingEntity.setLastAccessedDate(new Date());
            update(existingEntity);
        }
    }

    @Transactional
    public void markAsUserNotified(Long id) {
        DeprecatedApiUsageEntity entity = entityManager.find(DeprecatedApiUsageEntity.class, id);
        if (entity != null) {
            entity.setNotificationSent(new Date());
            update(entity);
        }
    }

    private void create(DeprecatedApiUsage apiUsage) {
        DeprecatedApiUsageEntity entity = new DeprecatedApiUsageEntity();
        entity.setApiCallCount(1L);
        entity.setApiKeyId(apiUsage.getApiKey().getId());
        entity.setHttpMethod(apiUsage.getHttpMethod());
        entity.setApiOperation(apiUsage.getApiOperation());
        entity.setResponseField(apiUsage.getResponseField());
        entity.setRemovalDate(apiUsage.getRemovalDate());
        entity.setMessage(apiUsage.getMessage());
        entity.setDeleted(false);
        entity.setLastAccessedDate(new Date());
        create(entity);
    }

    private DeprecatedApiUsageEntity getDeprecatedApiUsage(Long apiKeyId, String httpMethod,
            String apiOperation, String responseField) {
        String hql = "SELECT apiUsage "
                + "FROM DeprecatedApiUsageEntity apiUsage "
                + "JOIN FETCH apiUsage.apiKey apiKey "
                + "WHERE apiUsage.notificationSent IS NULL "
                + "AND apiUsage.deleted = false "
                + "AND apiUsage.apiKeyId = :apiKeyId "
                + "AND apiUsage.httpMethod = :httpMethod "
                + "AND apiUsage.apiOperation = :apiOperation ";
        if (!StringUtils.isEmpty(responseField)) {
                hql +=  "AND apiUsage.responseField = :responseField";
        } else {
            hql += "AND apiUsage.responseField IS NULL";
        }
        Query query = entityManager.createQuery(hql);
        query.setParameter("apiKeyId", apiKeyId);
        query.setParameter("httpMethod", httpMethod);
        query.setParameter("apiOperation", apiOperation);
        if (!StringUtils.isEmpty(responseField)) {
            query.setParameter("responseField", responseField);
        }
        List<DeprecatedApiUsageEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            LOGGER.info("No deprecated api usage exists for api key ID " + apiKeyId
                    + ", HTTP Method " + httpMethod
                    + ", API Operation " + apiOperation
                    + ", and Response Field " + responseField);
            return null;
        }
        return results.get(0);
    }
}
