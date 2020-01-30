package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Data access object for the certified_product_details view.
 * @author TYoung
 *
 */
@Repository(value = "certifiedProductSearchResultDAO")
public class CertifiedProductSearchResultDAO extends BaseDAOImpl {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductSearchResultDAO.class);

    public CertifiedProductDetailsDTO getById(Long productId) throws EntityRetrievalException {

        CertifiedProductDetailsDTO dto = null;
        CertifiedProductDetailsEntity entity = getEntityById(productId);

        if (entity != null) {
            dto = new CertifiedProductDetailsDTO(entity);
        } else {
            String msg = msgUtil.getMessage("listing.notFound");
            LOGGER.error("Error retreiving listing with ID " + productId + ": " + msg);
            throw new EntityRetrievalException(msg);
        }
        return dto;
    }

    @Transactional
    public List<CertifiedProductDetailsDTO> getByChplProductNumber(String chplProductNumber)
            throws EntityRetrievalException {

        List<CertifiedProductDetailsDTO> dtos = new ArrayList<CertifiedProductDetailsDTO>();
        List<CertifiedProductDetailsEntity> entities = getEntityByChplProductNumber(chplProductNumber);
        if (entities != null) {
            for (CertifiedProductDetailsEntity entity : entities) {
                dtos.add(new CertifiedProductDetailsDTO(entity));
            }
        }

        return dtos;
    }

    private List<CertifiedProductDetailsEntity> getEntityByChplProductNumber(String chplProductNumber)
            throws EntityRetrievalException {
        // List<CertifiedProductDetailsEntity> entity = new ArrayList<CertifiedProductDetailsEntity>();
        Query query = entityManager.createQuery(
                "SELECT deets " + "FROM CertifiedProductDetailsEntity deets " + "LEFT OUTER JOIN FETCH deets.product "
                        + "WHERE (deets.chplProductNumber = :chplProductNumber) " + "AND deets.deleted <> true",
                        CertifiedProductDetailsEntity.class);
        query.setParameter("chplProductNumber", chplProductNumber);

        @SuppressWarnings("unchecked") List<CertifiedProductDetailsEntity> result = query.getResultList();

        return result;

    }

    private CertifiedProductDetailsEntity getEntityById(Long entityId) throws EntityRetrievalException {
        CertifiedProductDetailsEntity entity = null;
        Query query = entityManager.createQuery(
                "SELECT DISTINCT deets "
                        + "FROM CertifiedProductDetailsEntity deets "
                        + "LEFT OUTER JOIN FETCH deets.product "
                        + "WHERE (deets.id = :entityid) "
                        + "AND deets.deleted <> true",
                        CertifiedProductDetailsEntity.class);
        query.setParameter("entityid", entityId);

        @SuppressWarnings("unchecked") List<CertifiedProductDetailsEntity> result = query.getResultList();
        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate Certified Product id in database.");
        }
        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}
