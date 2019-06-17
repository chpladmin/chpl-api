package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * The implementation for CertifiedProductSearchResultDAO.
 * @author TYoung
 *
 */
@Repository(value = "certifiedProductSearchResultDAO")
public class CertifiedProductSearchResultDAOImpl extends BaseDAOImpl implements CertifiedProductSearchResultDAO {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductSearchResultDAOImpl.class);

    @Override
    public CertifiedProductDetailsDTO getById(final Long productId) throws EntityRetrievalException {

        CertifiedProductDetailsDTO dto = null;
        CertifiedProductDetailsEntity entity = getEntityById(productId);

        if (entity != null) {
            dto = new CertifiedProductDetailsDTO(entity);
        } else {
            String msg =  msgUtil.getMessage("listing.notFound");
            LOGGER.error("Error retreiving listing with ID " + productId + ": " + msg);
            throw new EntityRetrievalException(msg);
        }
        return dto;
    }

    @Override
    @Transactional
    public List<CertifiedProductDetailsDTO> getByChplProductNumber(final String chplProductNumber)
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

    private List<CertifiedProductDetailsEntity> getEntityByChplProductNumber(final String chplProductNumber)
            throws EntityRetrievalException {
        //List<CertifiedProductDetailsEntity> entity = new ArrayList<CertifiedProductDetailsEntity>();
        Query query = entityManager.createQuery(
                "SELECT deets " + "FROM CertifiedProductDetailsEntity deets " + "LEFT OUTER JOIN FETCH deets.product "
                        + "WHERE (deets.chplProductNumber = :chplProductNumber) " + "AND deets.deleted <> true",
                CertifiedProductDetailsEntity.class);
        query.setParameter("chplProductNumber", chplProductNumber);

        List<CertifiedProductDetailsEntity> result = query.getResultList();

        return result;

    }

    private CertifiedProductDetailsEntity getEntityById(final Long entityId) throws EntityRetrievalException {
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
