package gov.healthit.chpl.dao;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProductChplProductNumberHistory;
import gov.healthit.chpl.entity.listing.CertifiedProductChplProductNumberHistoryEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.DateUtil;

@Component("certifiedProductChplProductNumberHistoryDao")
public class CertifiedProductChplProductNumberHistoryDao extends BaseDAOImpl {

    public Long createChplProductNumberHistoryMapping(Long listingId, CertifiedProductChplProductNumberHistory historyItem) throws EntityCreationException {
        try {
            CertifiedProductChplProductNumberHistoryEntity entity = new CertifiedProductChplProductNumberHistoryEntity();
            entity.setCertifiedProductId(listingId);
            entity.setChplProductNumber(historyItem.getChplProductNumber());
            entity.setEndDate(new Date(DateUtil.toEpochMillis(historyItem.getEndDateTime())));
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            create(entity);
            return entity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public List<CertifiedProductChplProductNumberHistory> getHistoricalChplProductNumbers(Long listingId) throws EntityRetrievalException {
        List<CertifiedProductChplProductNumberHistoryEntity> entities = getEntitiesByListingId(listingId);
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }


    private List<CertifiedProductChplProductNumberHistoryEntity> getEntitiesByListingId(Long listingId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery("SELECT hist "
                + "FROM CertifiedProductChplProductNumberHistoryEntity hist "
                + "WHERE hist.certifiedProductId = :listingId "
                + "AND hist.deleted = false",
                CertifiedProductChplProductNumberHistoryEntity.class);
        query.setParameter("listingId", listingId);
        return query.getResultList();
    }
}
