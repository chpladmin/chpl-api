package gov.healthit.chpl.servicebaseurllist;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;

@Service("serviceBaseUrlListByDeveloperDao")
public class ServiceBaseUrlListByDeveloperDao extends BaseDAOImpl {

    public List<ServiceBaseUrlListByDeveloper> getSbulUrls(Long developerId) {
        Query query = entityManager.createQuery("SELECT urls "
                + "FROM ServiceBaseUrlListUrlsByDeveloperEntity urls "
                + "WHERE urls.developerId = :developerId ",
                ServiceBaseUrlListUrlsByDeveloperEntity.class);
        query.setParameter("developerId", developerId);

        List<ServiceBaseUrlListUrlsByDeveloperEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }
}
