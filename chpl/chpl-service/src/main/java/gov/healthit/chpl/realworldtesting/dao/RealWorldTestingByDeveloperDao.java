package gov.healthit.chpl.realworldtesting.dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUrlByDeveloper;
import gov.healthit.chpl.realworldtesting.entity.RealWorldTestingPlansUrlsByDeveloper;
import gov.healthit.chpl.realworldtesting.entity.RealWorldTestingResultsUrlsByDeveloper;

@Service("realWorldTestingByDeveloperDao")
public class RealWorldTestingByDeveloperDao extends BaseDAOImpl {

    public List<RealWorldTestingUrlByDeveloper> getPlansUrls(Long developerId) {
        Query query = entityManager.createQuery("SELECT urls "
                + "FROM RealWorldTestingPlansUrlsByDeveloper urls "
                + "WHERE urls.developerId = :developerId",
                RealWorldTestingPlansUrlsByDeveloper.class);
        query.setParameter("developerId", developerId);

        List<RealWorldTestingPlansUrlsByDeveloper> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public List<RealWorldTestingUrlByDeveloper> getResultsUrls(Long developerId) {
        Query query = entityManager.createQuery("SELECT urls "
                + "FROM RealWorldTestingResultsUrlsByDeveloper urls "
                + "WHERE urls.developerId = :developerId",
                RealWorldTestingResultsUrlsByDeveloper.class);
        query.setParameter("developerId", developerId);

        List<RealWorldTestingResultsUrlsByDeveloper> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }
}
