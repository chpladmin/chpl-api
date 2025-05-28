package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class CriterionNotUpToDateReasonDao extends BaseDAOImpl {

    public List<CriterionNotUpToDateReason> getAll() {
        String hql = "SELECT reason "
                + "FROM ListingNotUpToDateReasonEntity reason "
                + "WHERE deleted = false ";
        List<CriterionNotUpToDateReasonEntity> entities = entityManager.createQuery(hql).getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public CriterionNotUpToDateReason getByName(String name) {
        String hql = "SELECT reason "
                + "FROM ListingNotUpToDateReasonEntity reason "
                + "WHERE deleted = false "
                + "AND name = :name";
        Query query = entityManager.createQuery(hql);
        query.setParameter("name", name);
        List<CriterionNotUpToDateReasonEntity> entities = query.getResultList();
        if (CollectionUtils.isEmpty(entities)) {
            LOGGER.error("No reason found with name " + name);
            return null;
        }
        return entities.get(0).toDomain();
    }
}
