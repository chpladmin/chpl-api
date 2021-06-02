package gov.healthit.chpl.scheduler.job.versionActivity;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.entity.ProductInsertableOwnerEntity;

@Repository("productOwnerHistoryDao")
public class ProductOwnerHistoryDao extends BaseDAOImpl {

    @Transactional(readOnly = true)
    public List<ProductOwnerDTO> getProductOwnerHistoryAsOfDate(Long productId, Date date) {
        String queryStr = "SELECT owner "
                + "FROM ProductInsertableOwnerEntity owner "
                + "WHERE owner.productId = :productId "
                + "AND (owner.deleted = false "
                + "OR (owner.deleted = true AND owner.lastModifiedDate >= :date)) ";

        Query query = entityManager.createQuery(queryStr, ProductInsertableOwnerEntity.class);
        query.setParameter("productId", productId);
        query.setParameter("date", date);
        List<ProductInsertableOwnerEntity> results = query.getResultList();

        return results.stream()
                .map(result -> new ProductOwnerDTO(result))
                .collect(Collectors.toList());
    }
}
