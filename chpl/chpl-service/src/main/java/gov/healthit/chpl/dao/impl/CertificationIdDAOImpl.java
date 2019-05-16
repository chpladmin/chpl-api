package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationIdDAO;
import gov.healthit.chpl.dto.CQMMetDTO;
import gov.healthit.chpl.dto.CertificationIdAndCertifiedProductDTO;
import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.entity.CertificationIdAndCertifiedProductEntity;
import gov.healthit.chpl.entity.CertificationIdEntity;
import gov.healthit.chpl.entity.CertificationIdProductMapEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

/**
 * Certification ID Data Access Object.
 * @author alarned
 *
 */
@Repository("certificationIdDAO")
public class CertificationIdDAOImpl extends BaseDAOImpl implements CertificationIdDAO {
    private static final Logger LOGGER = LogManager.getLogger(CertificationIdDAOImpl.class);

    // Note that in the ALPHA string the characters O and I have been removed.
    // This is to
    // prevent confusion of characters. So characters that may appear to be I/1
    // or O/0 will
    // always be numeric 1 and 0.
    //
    // The number of possible combinations of IDs within a specific
    // certification year is 10^34.
    private static final String CERT_ID_CHARS_ALPHA = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String CERT_ID_CHARS_NUMERIC = "0123456789";
    private static final String CERT_ID_CHARS = CERT_ID_CHARS_NUMERIC + CERT_ID_CHARS_ALPHA;
    private static final int CERT_ID_LENGTH = 15;
    private static final long MODIFIED_USER_ID = -4L;
    private static final int MAX_COUNT_ALPHAS = 3;

    private static final int ENCODED_RADIX = 36; // The radix base for values within
    // the Key
    private static final int ENCODED_PADDED_LENGTH = 8; // The number of digits for
    // each value in the Key

    @Override
    @Transactional
    public CertificationIdDTO create(final List<Long> productIds, final String year) throws EntityCreationException {
        CertificationIdEntity entity = null;
        CertificationIdDTO newDto = null;

        // Create a new EHR Certification ID record
        entity = new CertificationIdEntity();
        entity.setCertificationId(this.generateCertificationIdString(year));
        entity.setYear(year);
        entity.setKey(this.encodeCollectionKey(productIds));
        entity.setLastModifiedDate(new Date());
        entity.setCreationDate(new Date());
        entity.setLastModifiedUser(MODIFIED_USER_ID);
        entity.setPracticeTypeId(null);

        // Store the map entities
        entityManager.persist(entity);
        try {
            entity = getEntityByCertificationId(entity.getCertificationId());
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException("Unable to create Certification ID and Product Map.");
        }
        newDto = new CertificationIdDTO(entity);

        // Create map records
        for (Long prodId : productIds) {
            CertificationIdProductMapEntity mapEntity = new CertificationIdProductMapEntity();
            mapEntity.setCertifiedProductId(prodId);
            mapEntity.setCertificationIdId(newDto.getId());
            mapEntity.setLastModifiedDate(new Date());
            mapEntity.setCreationDate(new Date());
            mapEntity.setLastModifiedUser(MODIFIED_USER_ID);
            entityManager.persist(mapEntity);
        }

        // Store the map entities
        entityManager.flush();

        return newDto;
    }

    @Override
    @Transactional
    public CertificationIdDTO create(final CertificationIdDTO dto) throws EntityCreationException {

        CertificationIdEntity entity = null;
        try {
            if (null != dto.getId()) {
                entity = this.getEntityById(dto.getId());
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this record ID or Certification ID already exists.");
        } else {

            entity = new CertificationIdEntity();
            entity.setCertificationId(dto.getCertificationId());
            entity.setYear(dto.getYear());
            entity.setPracticeTypeId(dto.getPracticeTypeId());

            if (dto.getLastModifiedUser() != null) {
                entity.setLastModifiedUser(dto.getLastModifiedUser());
            } else {
                entity.setLastModifiedUser(AuthUtil.getAuditId());
            }

            if (dto.getLastModifiedDate() != null) {
                entity.setLastModifiedDate(dto.getLastModifiedDate());
            } else {
                entity.setLastModifiedDate(new Date());
            }

            if (dto.getCreationDate() != null) {
                entity.setCreationDate(dto.getCreationDate());
            } else {
                entity.setCreationDate(new Date());
            }

            create(entity);
            return new CertificationIdDTO(entity);
        }

    }

    @Override
    public List<String> getCriteriaNumbersMetByCertifiedProductIds(final List<Long> productIds) {
        List<String> results = new ArrayList<String>();
        if ((null != productIds) && (productIds.size() > 0)) {
            Query query = entityManager.createQuery(
                    "SELECT number FROM CertificationResultDetailsEntity "
                            + "WHERE success = TRUE AND deleted = FALSE AND certified_product_id IN :productIds "
                            + "GROUP BY number",
                            String.class);
            query.setParameter("productIds", productIds);
            results = query.getResultList();
        }
        return results;
    }

    @Override
    public List<CQMMetDTO> getCqmsMetByCertifiedProductIds(final List<Long> productIds) {
        List<CQMMetDTO> dtos = new ArrayList<CQMMetDTO>();
        if ((null != productIds) && (productIds.size() > 0)) {
            Query query = entityManager.createQuery(
                    "SELECT new gov.healthit.chpl.dto.CQMMetDTO(crde.cmsId, crde.version, crde.domain) "
                    + "FROM CQMResultDetailsEntity AS crde"
                            + " WHERE success = TRUE AND deleted = FALSE AND certifiedProductId IN :productIds "
                            + " AND crde.cmsId IS NOT NULL" + " GROUP BY crde.cmsId, crde.version, crde.domain");
            query.setParameter("productIds", productIds);
            dtos = query.getResultList();
        }

        return dtos;
    }

    @Override
    public List<CertificationIdDTO> findAll() {

        List<CertificationIdEntity> entities = getAllEntities();
        List<CertificationIdDTO> dtos = new ArrayList<>();

        for (CertificationIdEntity entity : entities) {
            CertificationIdDTO dto = new CertificationIdDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public CertificationIdDTO getById(final Long id) throws EntityRetrievalException {

        CertificationIdEntity entity = getEntityById(id);
        if (entity == null) {
            return null;
        }
        CertificationIdDTO dto = new CertificationIdDTO(entity);
        return dto;

    }

    @Override
    public CertificationIdDTO getByCertificationId(final String certificationId) throws EntityRetrievalException {

        CertificationIdEntity entity = getEntityByCertificationId(certificationId);
        if (entity == null) {
            return null;
        }
        CertificationIdDTO dto = new CertificationIdDTO(entity);
        return dto;

    }

    @Override
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

    @Override
    public List<Long> getProductIdsById(final Long id) throws EntityRetrievalException {

        Query query = entityManager.createQuery(
                "select certifiedProductId from CertificationIdProductMapEntity where certificationIdId = :id ",
                Long.class);
        query.setParameter("id", id);
        List<Long> queryResult = query.getResultList();
        return queryResult;

    }

    @Override
    public CertificationIdDTO getByProductIds(final List<Long> productIds, final String year)
            throws EntityRetrievalException {

        CertificationIdEntity entity = getEntityByProductIds(productIds, year);
        if (entity == null) {
            return null;
        }
        CertificationIdDTO dto = new CertificationIdDTO(entity);
        return dto;

    }

    @Override
    public Map<String, Boolean> verifyByCertificationId(final List<String> certificationIds)
            throws EntityRetrievalException {
        Map<String, Boolean> results = new HashMap<String, Boolean>();

        Query query = entityManager.createQuery("from CertificationIdEntity where certification_id IN :certids ",
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

    private void create(final CertificationIdEntity entity) {

        entityManager.persist(entity);
        entityManager.flush();
    }

    private List<CertificationIdEntity> getAllEntities() {

        List<CertificationIdEntity> result = entityManager
                .createQuery("from CertificationIdEntity ", CertificationIdEntity.class).getResultList();
        return result;

    }

    private CertificationIdEntity getEntityById(final Long id) throws EntityRetrievalException {

        CertificationIdEntity entity = null;

        Query query = entityManager.createQuery(
                "from CertificationIdEntity where (ehr_certification_id_id = :entityid) ", CertificationIdEntity.class);
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

    private CertificationIdEntity getEntityByCertificationId(final String certificationId)
            throws EntityRetrievalException {

        CertificationIdEntity entity = null;

        Query query = entityManager.createQuery("from CertificationIdEntity where (certification_id = :certid) ",
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

    private CertificationIdEntity getEntityByProductIds(final List<Long> productIds, final String year)
            throws EntityRetrievalException {

        CertificationIdEntity entity = null;

        // Lookup the EHR Certification ID record by:
        // 1. Looking up all CertificationIDs that are associated with the
        // products.
        // 2. Reduce the set by removing records that contain products other
        // than those specified.
        // 3. Make sure the number of products for the CertID matches the number
        // of products specified,
        // this filters out CertIDs that only contain a subset of those products
        // specified.
        List<CertificationIdEntity> result = new ArrayList<CertificationIdEntity>();
        Query query = entityManager.createQuery(

                "from CertificationIdEntity " + "where ehr_certification_id_id in ("
                + "select mpx.certificationIdId " + "from CertificationIdProductMapEntity as mpx "
                + "where mpx.certifiedProductId in :productIds " + "and mpx.certificationIdId not in ( "
                + "select mpa.certificationIdId " + "from CertificationIdProductMapEntity as mpa "
                + "where mpa.certificationIdId in ( " + "select mpy.certificationIdId "
                + "from CertificationIdProductMapEntity as mpy "
                + "where mpy.certifiedProductId in :productIds " + "group by mpy.certificationIdId " + ") "
                + "and mpa.certifiedProductId not in :productIds " + "group by mpa.certificationIdId " + ") "
                + "group by mpx.certificationIdId " + "having count(mpx.certificationIdId) = :productCount "
                + ") " + "and year = :year "
                + "order by creation_date DESC ",
                CertificationIdEntity.class);

        query.setParameter("productIds", productIds);
        query.setParameter("productCount", Long.valueOf(productIds.size()));
        query.setParameter("year", year);
        result = query.getResultList();
        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private static String encodeCollectionKey(final List<Long> numbers) {

        // Sort the product numbers before we encode them so they are in order
        Collections.sort(numbers);

        // Collect encoded version of all numbers.
        StringBuilder numbersString = new StringBuilder();
        for (Long number : numbers) {
            StringBuffer encodedNumber = new StringBuffer(Long.toString(number, ENCODED_RADIX));
            while (encodedNumber.length() < ENCODED_PADDED_LENGTH) {
                encodedNumber.insert(0, "0");
            }
            numbersString.append(encodedNumber);
        }

        return numbersString.toString().toUpperCase();
    }

    private List<CertificationIdAndCertifiedProductEntity> getAllCertificationIdsWithProductsEntities() {

        return entityManager.createQuery("from CertificationIdAndCertifiedProductEntity",
                CertificationIdAndCertifiedProductEntity.class).getResultList();
    }

    private static String generateCertificationIdString(final String year) {
        // Form the EHR Certification ID prefix and edition year identifier.
        // The identifier begins with the two-digit year followed by an "E" to
        // indicate
        // an edition year (e.g. "2015") or "H" to indicate a hybrid edition
        // year (e.g. "2014/2015").
        // To create it we take the last two digits of the year value which
        // would
        // represent the highest (current) year number...
        StringBuffer newId = new StringBuffer("00");
        newId.append(year.substring(year.length() - 2));

        // ...Decide if it's a hybrid year or not and attach the "E" or "H".
        if (-1 == year.indexOf("/")) {
            newId.append("E");
        } else {
            newId.append("H");
        }

        int suffixLength = (CERT_ID_LENGTH - newId.length());

        // Generate the remainder of the ID
        int alphaCount = 1;
        for (int i = 0; i < suffixLength; ++i) {
            char newChar = CERT_ID_CHARS.charAt(new Random().nextInt(CERT_ID_CHARS.length()));

            // In order to prevent words from forming within the ID, we do not
            // allow strings of
            // more than 3 sequential alpha characters. After 3 the next
            // character is forced to
            // to be numeric.

            // Check if newChar is numeric or alpha
            if (Pattern.matches("[0-9]", Character.toString(newChar))) {
                alphaCount = 0;
            } else {
                ++alphaCount;
                // If we've already had 3 alpha characters in a row, make the
                // next one numeric
                if (alphaCount > MAX_COUNT_ALPHAS) {
                    newChar = CERT_ID_CHARS_NUMERIC.charAt(new Random().nextInt(CERT_ID_CHARS_NUMERIC.length()));
                    alphaCount = 0;
                }
            }

            // Add newChar to Cert ID string
            newId.append(newChar);
        }

        // Safeguard we have a proper ID
        if (newId.length() != CERT_ID_LENGTH) {
            return null;
        }

        return newId.toString();
    }
}
