package gov.healthit.chpl.realworldtesting.dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUrlByDeveloper;
import gov.healthit.chpl.realworldtesting.entity.RealWorldTestingPlansUrlsByDeveloperEntity;
import gov.healthit.chpl.realworldtesting.entity.RealWorldTestingResultsUrlsByDeveloperEntity;

@Service("realWorldTestingByDeveloperDao")
public class RealWorldTestingByDeveloperDao extends BaseDAOImpl {

    public List<RealWorldTestingUrlByDeveloper> getPlansUrls(Long developerId) {
        Query query = entityManager.createQuery("SELECT urls "
                + "FROM RealWorldTestingPlansUrlsByDeveloperEntity urls "
                + "WHERE urls.developerId = :developerId "
                + "AND urls.rwtPlansUrl != NULL ",
                RealWorldTestingPlansUrlsByDeveloperEntity.class);
        query.setParameter("developerId", developerId);

        List<RealWorldTestingPlansUrlsByDeveloperEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public List<RealWorldTestingUrlByDeveloper> getResultsUrls(Long developerId) {
        Query query = entityManager.createQuery("SELECT urls "
                + "FROM RealWorldTestingResultsUrlsByDeveloperEntity urls "
                + "WHERE urls.developerId = :developerId "
                + "AND urls.rwtResultsUrl != NULL ",
                RealWorldTestingResultsUrlsByDeveloperEntity.class);
        query.setParameter("developerId", developerId);

        List<RealWorldTestingResultsUrlsByDeveloperEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }
}
