package gov.healthit.chpl.listing.measure;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.MeasureDomain;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository
public class MeasureDomainDAO extends BaseDAOImpl {
    public MeasureDomain findByDomain(String domain) throws EntityRetrievalException {
        MeasureDomainEntity entity = getByDomain(domain);
        if (entity != null) {
            return entity.convert();
        } else {
            return null;
        }
    }

    private MeasureDomainEntity getByDomain(String domain) throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "SELECT md "
                + "FROM MeasureDomainEntity md "
                + "WHERE md.deleted = false "
                + "AND md.domain = :domain ",
                MeasureDomainEntity.class);
        query.setParameter("domain", domain);
        List<MeasureDomainEntity> entities = query.getResultList();

        if (entities != null && entities.size() > 0) {
            return entities.get(0);
        } else {
            throw new EntityRetrievalException(String.format("Could not locate measure domain: %s", domain));
        }

    }
}
