package gov.healthit.chpl.certificationId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.SchedulerManager;
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
    public CertificationIdDTO getByListings(List<Long> listingIds, String year) {
        return certificationIdDao.getByListings(listingIds, year);
    }

    @Transactional(readOnly = true)
    public CertificationIdDTO getById(Long id) throws EntityRetrievalException {
        return certificationIdDao.getById(id);
    }

    @Transactional(readOnly = true)
    public CertificationIdDTO getByCertificationId(String certificationId) throws EntityRetrievalException {
        return certificationIdDao.getByCertificationId(certificationId);
    }

    public List<Long> getListingIdsByCertificationId(Long id) throws EntityRetrievalException {
        return certificationIdDao.getProductIdsById(id);
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
        return allCertificationIds.stream()
            .map(certId -> SimpleCertificationId.builder()
                    .certificationId(certId.getCertificationId())
                    .created(certId.getCreationDate())
                    .build())
            .collect(Collectors.toList());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_ID, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationIdDomainPermissions).GET_ALL_WITH_PRODUCTS)")
    @Transactional(readOnly = true)
    public List<SimpleCertificationId> getAllWithProducts() {
        //the key in this map is concatenated certification id and created millis
        //same as the hashcode and equals method use
        Map<String, SimpleCertificationId> results = new LinkedHashMap<String, SimpleCertificationId>();
        List<SimpleCertificationIdWithProducts> allCertificationIds = certificationIdDao.getAllCertificationIdsWithProducts();

        for (SimpleCertificationIdWithProducts certId : allCertificationIds) {
            String key = certId.getCertificationId() + certId.getCreated().getTime();
            if (results.containsKey(key)) {
                SimpleCertificationIdWithProducts currResult = (SimpleCertificationIdWithProducts) results.get(key);
                if (CollectionUtils.isEmpty(currResult.getProducts())) {
                    currResult.setProducts(certId.getProducts());
                } else {
                    currResult.getProducts().addAll(certId.getProducts());
                }
            } else {
                results.put(key, certId);
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
    public CertificationIdDTO create(List<Long> listingIds, String year) throws EntityCreationException {
        return certificationIdDao.create(listingIds, year);
    }
}
