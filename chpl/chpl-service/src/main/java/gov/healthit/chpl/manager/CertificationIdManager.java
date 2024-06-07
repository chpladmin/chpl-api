package gov.healthit.chpl.manager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationIdDAO;
import gov.healthit.chpl.domain.SimpleCertificationId;
import gov.healthit.chpl.domain.SimpleCertificationIdWithProducts;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.dto.CQMMetDTO;
import gov.healthit.chpl.dto.CertificationIdAndCertifiedProductDTO;
import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.scheduler.job.certificationId.CertificationIdEmailJob;
import gov.healthit.chpl.util.AuthUtil;

@Service
public class CertificationIdManager {
    private CertificationIdDAO certificationIdDao;
    private SchedulerManager schedulerManager;

    @Autowired
    public CertificationIdManager(CertificationIdDAO certificationIdDao,
             SchedulerManager schedulerManager) {
        this.certificationIdDao = certificationIdDao;
        this.schedulerManager = schedulerManager;
    }

    @Transactional(readOnly = true)
    public CertificationIdDTO getByListings(List<CertifiedProductDetailsDTO> listings, String year) throws EntityRetrievalException {
        return certificationIdDao.getByListings(listings, year);
    }

    @Transactional(readOnly = true)
    public CertificationIdDTO getById(Long id) throws EntityRetrievalException {
        return certificationIdDao.getById(id);
    }

    @Transactional(readOnly = true)
    public CertificationIdDTO getByCertificationId(String certificationId) throws EntityRetrievalException {
        return certificationIdDao.getByCertificationId(certificationId);
    }

    public List<Long> getProductIdsById(Long id) throws EntityRetrievalException {
        return certificationIdDao.getProductIdsById(id);
    }

    @Transactional(readOnly = true)
    public List<CertificationCriterion> getCriteriaMetByCertifiedProductIds(List<Long> productIds) {
        return certificationIdDao.getCriteriaMetByCertifiedProductIds(productIds);
    }

    @Transactional(readOnly = true)
    public List<CQMMetDTO> getCqmsMetByCertifiedProductIds(List<Long> productIds) {
        return certificationIdDao.getCqmsMetByCertifiedProductIds(productIds);
    }

    @Transactional(readOnly = true)
    public Map<String, Boolean> verifyByCertificationId(List<String> certificationIds) throws EntityRetrievalException {
        return certificationIdDao.verifyByCertificationId(certificationIds);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_ID, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationIdDomainPermissions).GET_ALL)")
    @Transactional(readOnly = true)
    public List<SimpleCertificationId> getAll() {
        List<SimpleCertificationId> results = new ArrayList<SimpleCertificationId>();
        List<CertificationIdDTO> allCertificationIds = certificationIdDao.findAll();
        for (CertificationIdDTO dto : allCertificationIds) {
            results.add(new SimpleCertificationId(dto));
        }
        return results;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_ID, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationIdDomainPermissions).GET_ALL_WITH_PRODUCTS)")
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

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_ID, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationIdDomainPermissions).GET_ALL)")
    @Transactional
    public ChplOneTimeTrigger triggerCmsIdReport() throws SchedulerException, ValidationException {
        ChplOneTimeTrigger complaintsReportTrigger = new ChplOneTimeTrigger();
        ChplJob complaintsReportJob = new ChplJob();
        complaintsReportJob.setName(CertificationIdEmailJob.JOB_NAME);
        complaintsReportJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(CertificationIdEmailJob.USER_KEY, AuthUtil.getCurrentUser());
        complaintsReportJob.setJobDataMap(jobDataMap);
        complaintsReportTrigger.setJob(complaintsReportJob);
        complaintsReportTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.FIVE_SECONDS_IN_MILLIS);
        complaintsReportTrigger = schedulerManager.createBackgroundJobTrigger(complaintsReportTrigger);
        return complaintsReportTrigger;
    }

    @Transactional(readOnly = false)
    public CertificationIdDTO create(List<CertifiedProductDetailsDTO> listings, String year) throws EntityCreationException, ActivityException {
        return certificationIdDao.create(listings, year);
    }
}
