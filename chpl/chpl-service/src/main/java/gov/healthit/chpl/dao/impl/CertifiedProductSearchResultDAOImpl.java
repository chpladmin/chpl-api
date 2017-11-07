package gov.healthit.chpl.dao.impl;

import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;

@Repository(value = "certifiedProductSearchResultDAO")
public class CertifiedProductSearchResultDAOImpl extends BaseDAOImpl implements CertifiedProductSearchResultDAO {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductSearchResultDAOImpl.class);

    @Override
    public CertifiedProductDetailsDTO getById(Long productId) throws EntityRetrievalException {

        CertifiedProductDetailsDTO dto = null;
        CertifiedProductDetailsEntity entity = getEntityById(productId);

        if (entity != null) {
            dto = new CertifiedProductDetailsDTO(entity);
        } else {
            String msg = String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.notFound"),
                    LocaleContextHolder.getLocale()));
            LOGGER.error("Error retreiving listing with ID " + productId + ": " + msg);
            throw new EntityRetrievalException(msg);
        }
        return dto;
    }

    private CertifiedProductDetailsEntity getEntityById(Long entityId) throws EntityRetrievalException {
        CertifiedProductDetailsEntity entity = null;
        Query query = entityManager.createQuery(
                "SELECT deets " + "FROM CertifiedProductDetailsEntity deets " + "LEFT OUTER JOIN FETCH deets.product "
                        + "WHERE (deets.id = :entityid) " + "AND deets.deleted <> true",
                CertifiedProductDetailsEntity.class);
        query.setParameter("entityid", entityId);

        List<CertifiedProductDetailsEntity> result = query.getResultList();
        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate Certified Product id in database.");
        }
        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}
