package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.entity.listing.CertificationResultDetailsEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlType;

@Repository(value = "certificationResultDetailsDAO")
public class CertificationResultDetailsDAO extends BaseDAOImpl {
    private static final Logger LOGGER = LogManager.getLogger(CertificationResultDetailsDAO.class);

    @Transactional
    public List<CertificationResultDetailsDTO> getCertificationResultDetailsByCertifiedProductId(
            final Long certifiedProductId) throws EntityRetrievalException {
        List<CertificationResultDetailsEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
        List<CertificationResultDetailsDTO> dtos = new ArrayList<CertificationResultDetailsDTO>(entities.size());

        for (CertificationResultDetailsEntity entity : entities) {
            dtos.add(new CertificationResultDetailsDTO(entity));
        }
        return dtos;
    }

    public List<CertificationResultDetailsDTO> getCertificationResultDetailsByCertifiedProductIdSED(
            final Long certifiedProductId) throws EntityRetrievalException {
        List<CertificationResultDetailsEntity> entities = getEntitiesByCertifiedProductIdSED(certifiedProductId);
        List<CertificationResultDetailsDTO> dtos = new ArrayList<CertificationResultDetailsDTO>(entities.size());

        for (CertificationResultDetailsEntity entity : entities) {
            dtos.add(new CertificationResultDetailsDTO(entity));
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public List<CertificationResultDetailsDTO> getByUrl(String url, UrlType urlType) {
        String queryStr = "SELECT crd "
                + "FROM CertificationResultDetailsEntity crd "
                + "JOIN FETCH crd.certificationCriterion cc "
                + "JOIN FETCH cc.certificationEdition "
                + "WHERE crd.deleted = false ";
        switch (urlType) {
        case API_DOCUMENTATION:
            queryStr += "AND crd.apiDocumentation = :url";
            break;
        case EXPORT_DOCUMENTATION:
            queryStr += "AND crd.exportDocumentation = :url";
            break;
        case DOCUMENTATION:
            queryStr += "AND crd.documentationUrl = :url";
            break;
        case USE_CASES:
            queryStr += "AND crd.useCases = :url";
            break;
        case SERVICE_BASE_URL_LIST:
            queryStr += "AND crd.serviceBaseUrlList = :url";
            break;
        default:
                break;
        }
        Query query = entityManager.createQuery(queryStr, CertificationResultDetailsEntity.class);
        query.setParameter("url", url);
        List<CertificationResultDetailsEntity> entities = query.getResultList();
        List<CertificationResultDetailsDTO> resultDtos = new ArrayList<CertificationResultDetailsDTO>();
        for (CertificationResultDetailsEntity entity : entities) {
            resultDtos.add(new CertificationResultDetailsDTO(entity));
        }
        return resultDtos;
    }

    public List<CertificationResultDetailsDTO> getCertificationResultsForSurveillanceListing(Surveillance surv) {
        List<CertificationResultDetailsDTO> certResults = null;
        if (surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null) {
            try {
                certResults = getCertificationResultDetailsByCertifiedProductId(surv.getCertifiedProduct().getId());
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not find cert results for certified product " + surv.getCertifiedProduct().getId(),
                        ex);
            }
        }
        return certResults;
    }

    private List<CertificationResultDetailsEntity> getEntitiesByCertifiedProductId(final Long productId)
            throws EntityRetrievalException {

        CertificationResultDetailsEntity entity = null;

        Query query = entityManager.createQuery(
                "SELECT crd FROM CertificationResultDetailsEntity crd "
                + "JOIN FETCH crd.certificationCriterion cc "
                + "JOIN FETCH cc.certificationEdition "
                + "WHERE crd.deleted = false "
                + "AND crd.certifiedProductId = :entityid ",
                CertificationResultDetailsEntity.class);
        query.setParameter("entityid", productId);
        List<CertificationResultDetailsEntity> result = query.getResultList();

        return result;
    }

    private List<CertificationResultDetailsEntity> getEntitiesByCertifiedProductIdSED(final Long productId)
            throws EntityRetrievalException {

        CertificationResultDetailsEntity entity = null;

        Query query = entityManager.createQuery(
                "SELECT crd FROM CertificationResultDetailsEntity crd "
                        + "JOIN FETCH crd.certificationCriterion cc "
                        + "JOIN FETCH cc.certificationEdition "
                        + "WHERE crd.deleted = false "
                        + "AND crd.certifiedProductId = :entityid "
                        + "AND crd.success = true "
                        + "AND crd.sed = true ",
                CertificationResultDetailsEntity.class);
        query.setParameter("entityid", productId);
        List<CertificationResultDetailsEntity> result = query.getResultList();

        return result;
    }
}
