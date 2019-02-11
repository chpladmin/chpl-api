package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertificationIdDAO;
import gov.healthit.chpl.domain.SimpleCertificationId;
import gov.healthit.chpl.domain.SimpleCertificationIdWithProducts;
import gov.healthit.chpl.domain.concept.ActivityConcept;
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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(CacheNames.ALL_CERT_IDS)
    /**
     * Should be secured at controller level for ROLE_ADMIN ||
     * ROLE_CMS_STAFF
     */
    public List<SimpleCertificationId> getAll() {
        return certificationIdDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(CacheNames.ALL_CERT_IDS_WITH_PRODUCTS)
    /**
     * Should be secured at controller level for ROLE_ADMIN || ROLE_ONC
     */
    public List<SimpleCertificationId> getAllWithProducts() {
        return certificationIdDao.getAllCertificationIdsWithProducts();
    }

    @Override
    @Transactional(readOnly = false)
    public CertificationIdDTO create(List<Long> productIds, String year)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {

        CertificationIdDTO result = certificationIdDao.create(productIds, year);

        String activityMsg = "CertificationId " + result.getCertificationId() + " was created.";
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATIONID, result.getId(), activityMsg, null,
                result);
        return result;
    }
}
