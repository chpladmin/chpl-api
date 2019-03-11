package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertificationIdDAO;
import gov.healthit.chpl.domain.SimpleCertificationId;
import gov.healthit.chpl.domain.SimpleCertificationIdWithProducts;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CQMMetDTO;
import gov.healthit.chpl.dto.CertificationIdAndCertifiedProductDTO;
import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationIdManager;

@Service
public class CertificationIdManagerImpl implements CertificationIdManager {

    @Autowired
    CertificationIdDAO certificationIdDao;

    @Autowired
    ActivityManager activityManager;

    @Override
    @Transactional(readOnly = true)
    public CertificationIdDTO getByProductIds(List<Long> productIds, String year) throws EntityRetrievalException {
        return certificationIdDao.getByProductIds(productIds, year);
    }

    @Override
    @Transactional(readOnly = true)
    public CertificationIdDTO getById(Long id) throws EntityRetrievalException {
        return certificationIdDao.getById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CertificationIdDTO getByCertificationId(String certificationId) throws EntityRetrievalException {
        return certificationIdDao.getByCertificationId(certificationId);
    }

    public List<Long> getProductIdsById(Long id) throws EntityRetrievalException {
        return certificationIdDao.getProductIdsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCriteriaNumbersMetByCertifiedProductIds(List<Long> productIds) {
        return certificationIdDao.getCriteriaNumbersMetByCertifiedProductIds(productIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CQMMetDTO> getCqmsMetByCertifiedProductIds(List<Long> productIds) {
        return certificationIdDao.getCqmsMetByCertifiedProductIds(productIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Boolean> verifyByCertificationId(List<String> certificationIds) throws EntityRetrievalException {
        return certificationIdDao.verifyByCertificationId(certificationIds);
    }

    /**
     * Should be secured at controller level for ROLE_CMS_STAFF
     * Get all cert ids.
     * Users of this class should call this method to get the cert ids
     * which should always be returned quickly from the cache.
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(CacheNames.ALL_CERT_IDS)
    public List<SimpleCertificationId> getAllCached() {
        return getAll();
    }

    /**
     * This method gets all the cert ids but does
     * not cache the result. This method should only be used for
     * filling in the pre-fetched cache and is here to avoid duplicating
     * manager logic elsewhere.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SimpleCertificationId> getAll() {
        List<SimpleCertificationId> results = new ArrayList<SimpleCertificationId>();
        List<CertificationIdDTO> allCertificationIds = certificationIdDao.findAll();
        for (CertificationIdDTO dto : allCertificationIds) {
            results.add(new SimpleCertificationId(dto));
        }
        return results;
    }

    /**
     * Should be secured at controller level for ROLE_ADMIN || ROLE_ONC
     * Get all cert ids and their associated listings.
     * Users of this class should call this method to get the cert ids
     * which should always be returned quickly from the cache.
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(CacheNames.ALL_CERT_IDS_WITH_PRODUCTS)
    public List<SimpleCertificationId> getAllWithProductsCached() {
        return getAllWithProducts();
    }

    /**
     * This method gets all the cert ids with products but does
     * not cache the result. This method should only be used for
     * filling in the pre-fetched cache and is here to avoid duplicating
     * manager logic elsewhere.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SimpleCertificationId> getAllWithProducts() {
        //the key in this map is concatenated certification id and created millis
        //same as the hashcode and equals method use
        Map<String, SimpleCertificationId> results = new LinkedHashMap<String, SimpleCertificationId>();
        List<CertificationIdAndCertifiedProductDTO> allCertificationIds = certificationIdDao
                .getAllCertificationIdsWithProducts();

        for (CertificationIdAndCertifiedProductDTO ehr : allCertificationIds) {
            SimpleCertificationId cert = new SimpleCertificationId();
            cert.setCertificationId(ehr.getCertificationId());
            cert.setCreated(ehr.getCreationDate());
            String key = ehr.getCertificationId() + ehr.getCreationDate().getTime();
            if (results.containsKey(key)) {
                SimpleCertificationIdWithProducts currResult = (SimpleCertificationIdWithProducts) results.get(key);
                if (StringUtils.isEmpty(currResult.getProducts())) {
                    currResult.setProducts(ehr.getChplProductNumber());
                } else {
                    String currProducts = currResult.getProducts();
                    currProducts = currProducts + ";" + ehr.getChplProductNumber();
                    currResult.setProducts(currProducts);
                }
            } else {
                SimpleCertificationIdWithProducts currResult = new SimpleCertificationIdWithProducts();
                currResult.setCertificationId(ehr.getCertificationId());
                currResult.setCreated(ehr.getCreationDate());
                currResult.setProducts(ehr.getChplProductNumber());
                results.put(key, currResult);
            }
        }

        return new ArrayList<SimpleCertificationId>(results.values());
    }

    @Override
    @Transactional(readOnly = false)
    public CertificationIdDTO create(final List<Long> productIds, final String year)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {

        CertificationIdDTO result = certificationIdDao.create(productIds, year);

        String activityMsg = "CertificationId " + result.getCertificationId() + " was created.";
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATIONID, result.getId(), activityMsg, null,
                result);
        return result;
    }
}
