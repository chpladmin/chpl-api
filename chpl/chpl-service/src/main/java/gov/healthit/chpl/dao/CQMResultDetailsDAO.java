package gov.healthit.chpl.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.entity.listing.CQMResultDetailsEntity;
import jakarta.persistence.Query;

@Repository(value = "cqmResultDetailsDAO")
public class CQMResultDetailsDAO extends BaseDAOImpl {

    @Transactional
    public List<CQMResultDetails> getCQMResultDetailsByCertifiedProductId(Long certifiedProductId) {
        List<CQMResultDetailsEntity> cqmResultEntities = getEntitiesByCertifiedProductId(certifiedProductId);
        return cqmResultEntities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    private List<CQMResultDetailsEntity> getEntitiesByCertifiedProductId(Long productId) {
        Query query = entityManager.createQuery(
                "FROM CQMResultDetailsEntity "
                + "WHERE (NOT deleted = true) "
                + "AND (certifiedProductId = :entityid) ",
                CQMResultDetailsEntity.class);
        query.setParameter("entityid", productId);
        List<CQMResultDetailsEntity> result = query.getResultList();
        return result;
    }

}
