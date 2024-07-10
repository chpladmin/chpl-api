package gov.healthit.chpl.scheduler.job.developer.inactive;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;

@Repository
public class InactiveProductDAO extends BaseDAOImpl {

    public List<InactiveProduct> getAll() {
        Query query = entityManager.createQuery("SELECT ip FROM InactiveProductEntity ip", InactiveProductEntity.class);
        List<InactiveProductEntity> results = query.getResultList();
        return results.stream()
                .map(result -> result.toDomain())
                .collect(Collectors.toList());
    }
}
