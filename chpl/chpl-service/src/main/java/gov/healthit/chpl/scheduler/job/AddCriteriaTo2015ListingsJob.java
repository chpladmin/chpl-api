package gov.healthit.chpl.scheduler.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Matcher;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.KeyMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.entity.MacraMeasureEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.ChplSchedulerReference;
import gov.healthit.chpl.scheduler.job.extra.JobResponseTriggerListener;
import gov.healthit.chpl.scheduler.job.extra.JobResponseTriggerWrapper;
import gov.healthit.chpl.util.AuthUtil;

public class AddCriteriaTo2015ListingsJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("addCriteriaToListingsJobLogger");
    private static final String JOB_NAME_FOR_EXISTING_LISTINGS = "addCriteriaToSingleListingJob";
    private static final String JOB_NAME_FOR_PENDING_LISTINGS = "addCriteriaToSinglePendingListingJob";
    private static final String JOB_GROUP = "subordinateJobs";

    @Autowired
    private CertificationCriterionDAO criterionDAO;

    @Autowired
    private InsertableMacraMeasureDao insertableMmDao;

    @Autowired
    private MacraMeasureDAO mmDao;

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private PendingCertifiedProductDaoIdsOnly pendingCertifiedProductDAO;

    @Autowired
    private ChplSchedulerReference chplScheduler;

    @Autowired
    private Environment env;

    private static final String CRITERIA_TO_ADD = "170.315 (b)(10);170.315 (d)(12);170.315 (d)(13);170.315 (g)(10)";

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        LOGGER.info("********* Starting the Add Criteria to 2015 Listings job. *********");

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setSecurityContext();
        LOGGER.info("statusInterval = " + jobContext.getMergedJobDataMap().getInt("statusInterval"));
        addCriteria();
        addMacraMeasureMaps();

        List<JobResponseTriggerWrapper> wrappers = new ArrayList<JobResponseTriggerWrapper>();
        wrappers.addAll(getExistingListingWrappers(jobContext));
        wrappers.addAll(getPendingListingWrappers(jobContext));

        LOGGER.info("Total number of listings to update: " + wrappers.size());

        try {
            JobResponseTriggerListener listener = new JobResponseTriggerListener(
                    wrappers,
                    jobContext.getMergedJobDataMap().getString("email"),
                    jobContext.getMergedJobDataMap().getString("emailCsvFileName"),
                    jobContext.getMergedJobDataMap().getString("emailSubject"),
                    jobContext.getMergedJobDataMap().getInt("statusInterval"),
                    env,
                    LOGGER);

            // Add the triggers and listener to the scheduler
            chplScheduler.getScheduler().getListenerManager()
                    .addTriggerListener(listener, getTriggerKeyMatchers(wrappers));

            // Fire the triggers
            wrappers.stream()
                    .forEach(wrapper -> fireTrigger(wrapper.getTrigger()));

        } catch (SchedulerException e) {
            LOGGER.error("Scheduler Error: " + e.getMessage(), e);
        }
        LOGGER.info("********* Completed the Add Criteria To 2015 Listings job. *********");
    }

    private void addCriteria() {
        add2015Criterion("170.315 (b)(10)", "Clinical Information Export");
        add2015Criterion("170.315 (d)(12)", "Encrypt Authentication Credentials");
        add2015Criterion("170.315 (d)(13)", "Multi-Factor Authentication");
        add2015Criterion("170.315 (g)(10)", "Standardized API for Patient and Population Services");
    }

    private void add2015Criterion(String number, String title) {
        CertificationCriterionDTO criterion = new CertificationCriterionDTO();
        criterion.setCertificationEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        criterion.setCertificationEditionId(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId());
        criterion.setNumber(number);
        criterion.setTitle(title);
        criterion.setRemoved(false);
        if (!criterionExists(criterion.getNumber())) {
            try {
                criterionDAO.create(criterion);
                LOGGER.info("Inserted criterion " + criterion.getNumber());
            } catch (EntityRetrievalException ex) {
                LOGGER.error("Error creating new 2015 criterion " + number, ex);
            } catch (EntityCreationException ex) {
                LOGGER.error("Error creating new 2015 criterion " + number, ex);
            }
        } else {
            LOGGER.info("Criterion " + number + " already exists.");
        }
    }

    private boolean criterionExists(String number) {
        CertificationCriterionDTO criterion =
                criterionDAO.getByNameAndYear(number, CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        return criterion != null;
    }

    private void addMacraMeasureMaps() {
        addMacraMeasureMap("170.315 (g)(10)", "RT2a EP Stage 2", "Patient Electronic Access: Eligible Professional", "Required Test 2: Stage 2 Objective 8 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT2a EP Stage 3", "Patient Electronic Access: Eligible Professional", "Required Test 2: Stage 3 Objective 5 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT2a EC ACI Transition", "Patient Electronic Access: Eligible Clinician", "Required Test 2: Promoting Interoperability Transition Objective 3 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT2a EC ACI", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Clinician", "Required Test 2: Promoting Interoperability Objective 3 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT2a EH/CAH Stage 2", "Patient Electronic Access: Eligible Hospital/Critical Access Hospital", "Required Test 2: Stage 2 Objective 8 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT2a EH/CAH Stage 3", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Hospital/Critical Access Hospital", "Required Test 2: Stage 3 Objective 5 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT2c EP Stage 2", "Patient Electronic Access: Eligible Professional", "'Required Test 2: Stage 2 Objective 8 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT2c EP Stage 3", "Patient Electronic Access: Eligible Professional", "Required Test 2: Stage 3 Objective 5 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT2c EC ACI Transition", "Patient Electronic Access: Eligible Clinician", "Required Test 2: Promoting Interoperability Transition Objective 3 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT2c EC ACI", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Clinician", "Required Test 2: Promoting Interoperability Objective 3 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT2c EH/CAH Stage 2", "Patient Electronic Access: Eligible Hospital/Critical Access Hospital", "Required Test 2: Stage 2 Objective 8 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT2c EH/CAH Stage 3", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Hospital/Critical Access Hospital", "Required Test 2: Stage 3 Objective 5 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT4a EP Stage 2", "View, Download, or Transmit (VDT): Eligible Professional", "Required Test 4: Stage 2 Objective 8 Measure 2");
        addMacraMeasureMap("170.315 (g)(10)", "RT4a EP Stage 3", "View, Download, or Transmit (VDT): Eligible Professional", "Required Test 4: Stage 3 Objective 6 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT4a EC ACI Transition", "View, Download, or Transmit (VDT): Eligible Clinician Group", "Required Test 4: Promoting Interoperability Transition Objective 3 Measure 2");
        addMacraMeasureMap("170.315 (g)(10)", "RT4a EC ACI", "View, Download, or Transmit (VDT):  Eligible Clinician", "Required Test 4: Promoting Interoperability Objective 4 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT4a EH/CAH Stage 2", "View, Download, or Transmit (VDT): Eligible Hospital/Critical Access Hospital", "Required Test 4: Stage 2 Objective 8 Measure 2");
        addMacraMeasureMap("170.315 (g)(10)", "RT4a EH/CAH Stage 3", "View, Download, or Transmit (VDT): Eligible Hospital/Critical Access Hospital", "Required Test 4: Stage 3 Objective 6 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT4c EP Stage 2", "View, Download, or Transmit (VDT): Eligible Professional", "Required Test 4: Stage 2 Objective 8 Measure 2");
        addMacraMeasureMap("170.315 (g)(10)", "RT4c EP Stage 3", "View, Download, or Transmit (VDT): Eligible Professional", "Required Test 4: Stage 3 Objective 6 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT4c EC ACI Transition", "View, Download, or Transmit (VDT):  Eligible Clinician", "Required Test 4: Promoting Interoperability Transition Objective 3 Measure 2");
        addMacraMeasureMap("170.315 (g)(10)", "RT4c EC ACI", "View, Download, or Transmit (VDT):  Eligible Clinician", "Required Test 4: Promoting Interoperability Objective 4 Measure 1");
        addMacraMeasureMap("170.315 (g)(10)", "RT4c EH/CAH Stage 2", "View, Download, or Transmit (VDT): Eligible Hospital/Critical Access Hospital", "Required Test 4: Stage 2 Objective 8 Measure 2");
        addMacraMeasureMap("170.315 (g)(10)", "RT4c EH/CAH Stage 3", "View, Download, or Transmit (VDT): Eligible Hospital/Critical Access Hospital", "Required Test 4: Stage 3 Objective 6 Measure 1");

    }

    private void addMacraMeasureMap(String criterionNumber, String value, String name, String description) {
        if (!macraMeasureCriteriaMapExists(criterionNumber, value)) {
            MacraMeasureDTO mm = new MacraMeasureDTO();
            CertificationCriterionDTO criterion = criterionDAO.getByNameAndYear(criterionNumber, "2015");
            if (criterion == null) {
                LOGGER.error("Cannot insert macra measure for criteria that is not found: " + criterionNumber);
            } else {
                mm.setCriteriaId(criterion.getId());
                mm.setDescription(description);
                mm.setName(name);
                mm.setValue(value);
                insertableMmDao.create(mm);
                LOGGER.info("Inserted macra measure " + value + " for criterion " + criterion.getNumber());
            }
        } else {
            LOGGER.info("Mapping from " + criterionNumber + " to macra measure " + value + " already exists.");
        }
    }

    private boolean macraMeasureCriteriaMapExists(String criterionNumber, String value) {
        MacraMeasureDTO mm = mmDao.getByCriteriaNumberAndValue(criterionNumber, value);
        return mm != null;
    }

    private List<JobResponseTriggerWrapper> getExistingListingWrappers(JobExecutionContext jobContext) {
        List<Long> listings = getListingIds();
        List<JobResponseTriggerWrapper> wrappers = new ArrayList<JobResponseTriggerWrapper>();

        for (Long cpId : listings) {
            wrappers.add(buildTriggerWrapper(cpId, jobContext, JOB_NAME_FOR_EXISTING_LISTINGS));
        }
        LOGGER.info("Total number of existing listings to update: " + listings.size());
        return wrappers;
    }

    private List<JobResponseTriggerWrapper> getPendingListingWrappers(JobExecutionContext jobContext) {
        List<Long> listings = getPendingListingIds();
        List<JobResponseTriggerWrapper> wrappers = new ArrayList<JobResponseTriggerWrapper>();

        for (Long cpId : listings) {
            wrappers.add(buildTriggerWrapper(cpId, jobContext, JOB_NAME_FOR_PENDING_LISTINGS));
        }
        LOGGER.info("Total number of pending listings to update: " + listings.size());
        return wrappers;
    }

    private JobResponseTriggerWrapper buildTriggerWrapper(final Long cpId, final JobExecutionContext jobContext, String jobName) {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("listing", cpId);
        dataMap.put("criteria", CRITERIA_TO_ADD);
        dataMap.put("logger", LOGGER);

        return new JobResponseTriggerWrapper(
                TriggerBuilder.newTrigger()
                        .forJob(jobName, JOB_GROUP)
                        .usingJobData(dataMap)
                        .build());
    }

    private List<Matcher<TriggerKey>> getTriggerKeyMatchers(final List<JobResponseTriggerWrapper> wrappers) {
        return wrappers.stream()
                .map(wrapper -> KeyMatcher.keyEquals(wrapper.getTrigger().getKey()))
                .collect(Collectors.toList());
    }

    private void fireTrigger(final Trigger trigger) {
        try {
            chplScheduler.getScheduler().scheduleJob(trigger);
        } catch (SchedulerException e) {
            LOGGER.error("Scheduler Error: " + e.getMessage(), e);
        }
    }

    private List<Long> getListingIds() {
        List<CertifiedProductDetailsDTO> cps = certifiedProductDAO.findByEdition("2015");

        return cps.stream()
                .map(cp -> cp.getId())
                .filter(cp -> cp >= 10150L) //for testing purposes
                .collect(Collectors.toList());
    }

    @Transactional
    private List<Long> getPendingListingIds() {
        return pendingCertifiedProductDAO.getAllIds();
    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Component("insertableMacraMeasureDao")
    private static class InsertableMacraMeasureDao extends BaseDAOImpl {

        public InsertableMacraMeasureDao() {
            super();
        }

        @Transactional
        public void create(MacraMeasureDTO dto) {
            MacraMeasureEntity toInsert = new MacraMeasureEntity();
            toInsert.setCertificationCriterionId(dto.getCriteriaId());
            toInsert.setDeleted(false);
            toInsert.setDescription(dto.getDescription());
            toInsert.setLastModifiedUser(AuthUtil.getAuditId());
            toInsert.setLastModifiedDate(new Date());
            toInsert.setName(dto.getName());
            toInsert.setRemoved(false);
            toInsert.setValue(dto.getValue());
            super.create(toInsert);
        }
    }

    @Component("pendingCertifiedProductDaoIdsOnly")
    private static class PendingCertifiedProductDaoIdsOnly extends BaseDAOImpl {

        public PendingCertifiedProductDaoIdsOnly() {
            super();
        }

        @Transactional
        public List<Long> getAllIds() {
            List<PendingCertifiedProductEntity> entities = entityManager.createQuery(
                    "SELECT pcp from PendingCertifiedProductEntity pcp "
                            + "WHERE (not pcp.deleted = true)",
                            PendingCertifiedProductEntity.class)
                    .getResultList();
            List<Long> allIds = new ArrayList<Long>();
            for (PendingCertifiedProductEntity entity : entities) {
                allIds.add(entity.getId());
            }
            return allIds;
        }
    }
}
