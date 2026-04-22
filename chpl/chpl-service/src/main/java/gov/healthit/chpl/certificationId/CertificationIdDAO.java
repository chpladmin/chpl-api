package gov.healthit.chpl.certificationId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.persistence.Query;

@Repository("certificationIdDAO")
public class CertificationIdDAO extends BaseDAOImpl {
    private static final Logger LOGGER = LogManager.getLogger(CertificationIdDAO.class);
    // Note that in the ALPHA string the characters O and I have been removed.
    // This is to prevent confusion of characters. So characters that may appear to be I/1
    // or O/0 will always be numeric 1 and 0.
    //
    // The number of possible combinations of IDs within a specific certification year is 10^34.
    private static final String CERT_ID_CHARS_ALPHA = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String CERT_ID_CHARS_NUMERIC = "0123456789";
    private static final String CERT_ID_CHARS = CERT_ID_CHARS_NUMERIC + CERT_ID_CHARS_ALPHA;
    private static final int CERT_ID_LENGTH = 15;
    private static final String CERT_ID_15C_BEGIN = "0015C";
    private static final int MAX_COUNT_ALPHAS = 3;

    private CertificationIdYearCalculator certIdYearCalculator;

    @Autowired
    public CertificationIdDAO(CertificationIdYearCalculator certIdYearCalculator) {
        this.certIdYearCalculator = certIdYearCalculator;
    }

    @Transactional
    public CertificationIdDTO create(List<Long> listingIds, String year) throws EntityCreationException {
        CertificationIdEntity entity = null;
        CertificationIdDTO newDto = null;

        // Create a new EHR Certification ID record
        entity = new CertificationIdEntity();
        entity.setCertificationId(this.generateCertificationIdString(year));
        entity.setYear(year);
        entity.setPracticeTypeId(null);

        // Store the map entities
        entityManager.persist(entity);
        newDto = new CertificationIdDTO(entity);

        // Create map records
        for (Long listingId : listingIds) {
            CertificationIdProductMapEntity mapEntity = new CertificationIdProductMapEntity();
            mapEntity.setCertifiedProductId(listingId);
            mapEntity.setCertificationIdId(newDto.getId());
            entityManager.persist(mapEntity);
        }

        // Store the map entities
        entityManager.flush();

        return newDto;
    }

    public List<CertificationIdDTO> findAll() {

        List<CertificationIdEntity> entities = getAllEntities();
        List<CertificationIdDTO> dtos = new ArrayList<>();

        for (CertificationIdEntity entity : entities) {
            CertificationIdDTO dto = new CertificationIdDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public CertificationIdDTO getById(Long id) throws EntityRetrievalException {
        CertificationIdEntity entity = getEntityById(id);
        if (entity == null) {
            return null;
        }
        CertificationIdDTO dto = new CertificationIdDTO(entity);
        return dto;
    }

    public CertificationIdDTO getByCertificationId(String certificationId) throws EntityRetrievalException {
        CertificationIdEntity entity = getEntityByCertificationId(certificationId);
        if (entity == null) {
            return null;
        }
        CertificationIdDTO dto = new CertificationIdDTO(entity);
        return dto;
    }

    public List<CertificationIdAndCertifiedProductDTO> getAllCertificationIdsWithProducts() {
        LOGGER.debug("Starting query to get all certification ids with products.");
        List<CertificationIdAndCertifiedProductEntity> entities = getAllCertificationIdsWithProductsEntities();
        LOGGER.debug("Completed query to get all certification ids with products.");
        List<CertificationIdAndCertifiedProductDTO> results = new ArrayList<CertificationIdAndCertifiedProductDTO>();
        for (CertificationIdAndCertifiedProductEntity entity : entities) {
            CertificationIdAndCertifiedProductDTO dto = new CertificationIdAndCertifiedProductDTO(entity);
            results.add(dto);
        }
        return results;
    }

    public CertificationIdDTO getByListings(List<CertifiedProductDetailsDTO> listings, String year)
            throws EntityRetrievalException {
        CertificationIdEntity entity = getEntityByListings(listings, year);
        if (entity == null) {
            return null;
        }
        CertificationIdDTO dto = new CertificationIdDTO(entity);
        return dto;
    }

    public Map<String, Boolean> verifyByCertificationId(List<String> certificationIds)
            throws EntityRetrievalException {
        Map<String, Boolean> results = new HashMap<String, Boolean>();

        Query query = entityManager.createQuery("from CertificationIdEntity where certificationId IN :certids ",
                CertificationIdEntity.class);
        query.setParameter("certids", certificationIds);
        List<CertificationIdEntity> queryResult = query.getResultList();

        // Get the IDs that were found...
        for (CertificationIdEntity entity : queryResult) {
            results.put(entity.getCertificationId(), true);
        }

        // then merge in the IDs that where not found.
        for (String certId : certificationIds) {
            if (null == results.get(certId)) {
                results.put(certId, false);
            }
        }

        return results;
    }

    public List<Long> getProductIdsById(Long id) throws EntityRetrievalException {

        Query query = entityManager.createQuery(
                "select certifiedProductId from CertificationIdProductMapEntity where certificationIdId = :id ",
                Long.class);
        query.setParameter("id", id);
        List<Long> queryResult = query.getResultList();
        return queryResult;
    }

    public List<CertificationCriterion> getCriteriaMetByListingIds(List<Long> listingIds) {
        List<CertificationCriterionEntity> criterionEntities = new ArrayList<CertificationCriterionEntity>();
        if (!CollectionUtils.isEmpty(listingIds)) {
            Query query = entityManager.createQuery(
                    "SELECT crd.certificationCriterion FROM CertificationResultDetailsEntity crd "
                            + "WHERE crd.success = TRUE "
                            + "AND crd.deleted = FALSE "
                            + "AND crd.certifiedProductId IN (:listingIds)",
                            CertificationCriterionEntity.class);
            query.setParameter("listingIds", listingIds);
            criterionEntities = query.getResultList();
        }
        return criterionEntities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public List<CQMMetDTO> getCqmsMetByListingIds(List<Long> listingIds) {
        List<CQMMetDTO> cmqsMet = new ArrayList<CQMMetDTO>();
        if (!CollectionUtils.isEmpty(listingIds)) {
            Query query = entityManager.createQuery(
                    "SELECT new gov.healthit.chpl.certificationId.CQMMetDTO(crde.cmsId, crde.version, crde.domain) "
                            + "FROM CQMResultDetailsEntity AS crde"
                            + " WHERE success = TRUE AND deleted = FALSE AND certifiedProductId IN :listingIds "
                            + " AND crde.cmsId IS NOT NULL" + " GROUP BY crde.cmsId, crde.version, crde.domain");
            query.setParameter("listingIds", listingIds);
            cmqsMet = query.getResultList();
        }

        return cmqsMet;
    }

    private List<CertificationIdEntity> getAllEntities() {
        List<CertificationIdEntity> result = entityManager
                .createQuery("from CertificationIdEntity ", CertificationIdEntity.class).getResultList();
        return result;
    }

    private CertificationIdEntity getEntityById(Long id) throws EntityRetrievalException {
        CertificationIdEntity entity = null;

        Query query = entityManager.createQuery(
                "from CertificationIdEntity where (id = :entityid) ", CertificationIdEntity.class);
        query.setParameter("entityid", id);
        List<CertificationIdEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("certificationId.notFound");
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate certificationId id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }

    private CertificationIdEntity getEntityByCertificationId(String certificationId)
            throws EntityRetrievalException {
        CertificationIdEntity entity = null;

        Query query = entityManager.createQuery("from CertificationIdEntity where (certificationId = :certid) ",
                CertificationIdEntity.class);
        query.setParameter("certid", certificationId);
        List<CertificationIdEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("certificationId.notFound");
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate certificationId in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }

    private CertificationIdEntity getEntityByListings(List<CertifiedProductDetailsDTO> listings, String year)
            throws EntityRetrievalException {
        List<Long> productIds = listings.stream()
                .map(listing -> listing.getId())
                .toList();
        CertificationIdEntity entity = null;

        // Lookup the EHR Certification ID record by:
        // 1. Looking up all CertificationIDs that are associated with the products.
        // 2. Reduce the set by removing records that contain products other than those specified.
        // 3. Make sure the number of products for the CertID matches the number of products specified,
        // this filters out CertIDs that only contain a subset of those products specified.
        Query query = entityManager.createQuery(
                "FROM CertificationIdEntity "
                + "WHERE id in ("
                    + "SELECT mpx.certificationIdId "
                    + "FROM CertificationIdProductMapEntity as mpx "
                    + "WHERE mpx.certifiedProductId IN :productIds "
                    + "AND mpx.certificationIdId NOT IN ( "
                        + "SELECT mpa.certificationIdId "
                        + "FROM CertificationIdProductMapEntity as mpa "
                        + "WHERE mpa.certificationIdId IN ( "
                            + "SELECT mpy.certificationIdId "
                            + "FROM CertificationIdProductMapEntity as mpy "
                            + "WHERE mpy.certifiedProductId in :productIds "
                            + "GROUP BY mpy.certificationIdId " + ") "
                        + "AND mpa.certifiedProductId NOT IN :productIds "
                        + "GROUP BY mpa.certificationIdId " + ") "
                    + "GROUP BY mpx.certificationIdId "
                    + "HAVING COUNT(mpx.certificationIdId) = :productCount "
                + ") "
                + "AND year = :year "
                + "ORDER BY creationDate DESC ",
                CertificationIdEntity.class);

        query.setParameter("productIds", productIds);
        query.setParameter("productCount", Long.valueOf(productIds.size()));
        query.setParameter("year", year);
        List<CertificationIdEntity> results = query.getResultList();
        if (!CollectionUtils.isEmpty(results) && results.size() > 1) {
            //there could be more than one cert ID that matches for this set of products (15E and 15C)
            //if there is a 15C cert ID available, that is the one we want
            entity = get15CCertIdEntity(results);
        } else if (!CollectionUtils.isEmpty(results) && results.size() == 1) {
            entity = results.get(0);
        }
        return entity;
    }

    private CertificationIdEntity get15CCertIdEntity(List<CertificationIdEntity> entities) {
        Optional<CertificationIdEntity> entityWith15CCertId = entities.stream()
            .filter(entity -> entity.getCertificationId().startsWith(CERT_ID_15C_BEGIN))
            .findFirst();
        if (entityWith15CCertId.isEmpty()) {
            return null;
        }
        return entityWith15CCertId.get();
    }

    private List<CertificationIdAndCertifiedProductEntity> getAllCertificationIdsWithProductsEntities() {
        return entityManager.createQuery("from CertificationIdAndCertifiedProductEntity",
                CertificationIdAndCertifiedProductEntity.class).getResultList();
    }

    private String generateCertificationIdString(String year) throws EntityCreationException {
        StringBuffer newId = new StringBuffer();
        newId.append(getYearPartOfNewCertIdString(year));
        newId.append("C");

        int suffixLength = (CERT_ID_LENGTH - newId.length());
        int alphaCount = 1;
        for (int i = 0; i < suffixLength; ++i) {
            char newChar = CERT_ID_CHARS.charAt(new Random().nextInt(CERT_ID_CHARS.length()));

            // In order to prevent words from forming within the ID, we do not allow strings of
            // more than 3 sequential alpha characters. After 3 the next character is forced to to be numeric.
            if (Pattern.matches("[0-9]", Character.toString(newChar))) {
                alphaCount = 0;
            } else {
                ++alphaCount;
                if (alphaCount > MAX_COUNT_ALPHAS) {
                    newChar = CERT_ID_CHARS_NUMERIC.charAt(new Random().nextInt(CERT_ID_CHARS_NUMERIC.length()));
                    alphaCount = 0;
                }
            }
            newId.append(newChar);
        }

        if (newId.length() != CERT_ID_LENGTH) {
            return null;
        }

        return newId.toString();
    }

    private String getYearPartOfNewCertIdString(String year) {
        LocalDate now = LocalDate.now();
        //TODO: Remove with //OCD-4928
        if (now.isBefore(certIdYearCalculator.getInitialCmsIdTransitionToAnnualFormatDay())) {
            return "00" + year.substring(year.length() - 2);
        }
        return year;
    }
}
