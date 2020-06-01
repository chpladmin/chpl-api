package gov.healthit.chpl.scheduler.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.dto.TestFunctionalityCriteriaMapDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.entity.CertificationEditionEntity;
import gov.healthit.chpl.entity.MacraMeasureEntity;
import gov.healthit.chpl.entity.TestDataCriteriaMapEntity;
import gov.healthit.chpl.entity.TestDataEntity;
import gov.healthit.chpl.entity.TestFunctionalityCriteriaMapEntity;
import gov.healthit.chpl.entity.TestFunctionalityEntity;
import gov.healthit.chpl.entity.TestProcedureCriteriaMapEntity;
import gov.healthit.chpl.entity.TestProcedureEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.manager.TestingFunctionalityManager;
import gov.healthit.chpl.scheduler.ChplSchedulerReference;
import gov.healthit.chpl.scheduler.job.extra.JobResponseTriggerListener;
import gov.healthit.chpl.scheduler.job.extra.JobResponseTriggerWrapper;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CuresUpdateService;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.PrivacyAndSecurityCriteriaReviewer;
import net.sf.ehcache.CacheManager;

public class AddCriteriaTo2015ListingsJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("addCriteriaToListingsJobLogger");
    private static final String JOB_NAME_FOR_EXISTING_LISTINGS = "addCriteriaToSingleListingJob";
    private static final String JOB_NAME_FOR_PENDING_LISTINGS = "addCriteriaToSinglePendingListingJob";
    private static final String JOB_GROUP = "subordinateJobs";
    private static final long ADMIN_ID = -2L;

    private static final String CRITERIA_TO_ADD = "170.315 (b)(1):Transitions of Care (Cures Update);"
            + "170.315 (b)(2):Clinical Information Reconciliation and Incorporation (Cures Update);"
            + "170.315 (b)(3):Electronic Prescribing (Cures Update);"
            + "170.315 (b)(7):Security Tags - Summary of Care - Send (Cures Update);"
            + "170.315 (b)(8):Security Tags - Summary of Care - Receive (Cures Update);"
            + "170.315 (b)(9):Care Plan (Cures Update);"
            + "170.315 (b)(10):Electronic Health Information Export;"
            + "170.315 (c)(3):Clinical Quality Measures - Report (Cures Update);"
            + "170.315 (d)(2):Auditable Events and Tamper-Resistance (Cures Update);"
            + "170.315 (d)(3):Audit Report(s) (Cures Update);"
            + "170.315 (d)(10):Auditing Actions on Health Information (Cures Update);"
            + "170.315 (d)(12):Encrypt Authentication Credentials;"
            + "170.315 (d)(13):Multi-Factor Authentication;"
            + "170.315 (e)(1):View, Download, and Transmit to 3rd Party (Cures Update);"
            + "170.315 (f)(5):Transmission to Public Health Agencies - Electronic Case Reporting (Cures Update);"
            + "170.315 (g)(6):Consolidated CDA Creation Performance (Cures Update);"
            + "170.315 (g)(9):Application Access - All Data Request (Cures Update);"
            + "170.315 (g)(10):Standardized API for Patient and Population Services;";
    private static final String EDITION_2015 = "2015";
    private static final Long EDITION_2015_ID = 3L;

    @Autowired
    private CertificationCriterionDAO criterionDAO;

    @Autowired
    private InsertableMacraMeasureDao insertableMmDao;

    @Autowired
    private InsertableTestDataDao insertableTestDataDao;

    @Autowired
    private InsertableTestProcedureDao insertableTestProcDao;

    @Autowired
    private InsertableTestFunctionalityDao insertableTestFuncDao;

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private PendingCertifiedProductDaoIdsOnly pendingCertifiedProductDAO;

    @Autowired
    private ChplSchedulerReference chplScheduler;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private PendingCertifiedProductManager pcpManager;

    @Autowired
    private TestFunctionalityDAO testFunctionalityDAO;

    @Autowired
    private TestingFunctionalityManager testFuncManager;

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("privacyAndSecurityCriteriaReviewer")
    private PrivacyAndSecurityCriteriaReviewer privacyAndSecurityCriteriaReviewer;

    @Autowired
    @Qualifier("pendingPrivacyAndSecurityCriteriaReviewer")
    private gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.PrivacyAndSecurityCriteriaReviewer pendingPrivacyAndSecurityCriteriaReviewer;

    @Autowired
    private CuresUpdateService curesUpdateService;

    @Autowired
    private CertificationCriterionService certificationCriterionService;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        LOGGER.info("********* Starting the Add Criteria to 2015 Listings job. *********");

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setSecurityContext();
        LOGGER.info("statusInterval = " + jobContext.getMergedJobDataMap().getInt("statusInterval"));
        addCriteria();
        addTestDataMaps();
        addTestProcedureMaps();
        addTestFunctionalities();
        addTestFunctionalityMaps();
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
        } finally {
            // search options-related calls may have changed data now
            CacheManager.getInstance().clearAll();
            // Need to "refresh" the data in CertifiedProductDetailsManager since it is stored within the bean.
            certifiedProductDetailsManager.refreshData();
            pcpManager.refreshData();
            testFuncManager.onApplicationEvent(null);

            pendingPrivacyAndSecurityCriteriaReviewer.postConstruct();
            privacyAndSecurityCriteriaReviewer.postConstruct();

            certificationCriterionService.postConstruct();
            curesUpdateService.postConstruct();
        }
        LOGGER.info("********* Completed the Add Criteria To 2015 Listings job. *********");
    }

    private void addCriteria() {
        add2015Criterion("170.315 (b)(1)", "Transitions of Care (Cures Update)");
        add2015Criterion("170.315 (b)(2)", "Clinical Information Reconciliation and Incorporation (Cures Update)");
        add2015Criterion("170.315 (b)(3)", "Electronic Prescribing (Cures Update)");
        add2015Criterion("170.315 (b)(7)", "Security Tags - Summary of Care - Send (Cures Update)");
        add2015Criterion("170.315 (b)(8)", "Security Tags - Summary of Care - Receive (Cures Update)");
        add2015Criterion("170.315 (b)(9)", "Care Plan (Cures Update)");
        add2015Criterion("170.315 (b)(10)", "Electronic Health Information Export");
        add2015Criterion("170.315 (c)(3)", "Clinical Quality Measures - Report (Cures Update)");
        add2015Criterion("170.315 (d)(2)", "Auditable Events and Tamper-Resistance (Cures Update)");
        add2015Criterion("170.315 (d)(3)", "Audit Report(s) (Cures Update)");
        add2015Criterion("170.315 (d)(10)", "Auditing Actions on Health Information (Cures Update)");
        add2015Criterion("170.315 (d)(12)", "Encrypt Authentication Credentials");
        add2015Criterion("170.315 (d)(13)", "Multi-Factor Authentication");
        add2015Criterion("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)");
        add2015Criterion("170.315 (f)(5)", "Transmission to Public Health Agencies - Electronic Case Reporting (Cures Update)");
        add2015Criterion("170.315 (g)(6)", "Consolidated CDA Creation Performance (Cures Update)");
        add2015Criterion("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)");
        add2015Criterion("170.315 (g)(10)", "Standardized API for Patient and Population Services");
    }

    private void add2015Criterion(String number, String title) {
        CertificationCriterionDTO criterion = new CertificationCriterionDTO();
        criterion.setCertificationEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        criterion.setCertificationEditionId(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId());
        criterion.setNumber(number);
        criterion.setTitle(title);
        criterion.setRemoved(false);
        if (!criterionExists(criterion.getNumber(), criterion.getTitle())) {
            try {
                criterionDAO.create(criterion);
                LOGGER.info("Inserted criterion " + criterion.getNumber() + ":" + criterion.getTitle());
            } catch (EntityRetrievalException ex) {
                LOGGER.error("Error creating new 2015 criterion " + number + ":" + title, ex);
            } catch (EntityCreationException ex) {
                LOGGER.error("Error creating new 2015 criterion " + number + ":" + title, ex);
            }
        } else {
            LOGGER.info("Criterion " + number + ":" + title + " already exists.");
        }
    }

    private boolean criterionExists(String number, String title) {
        CertificationCriterionDTO criterion = criterionDAO.getByNumberAndTitle(number, title);
        return criterion != null;
    }

    @SuppressWarnings({"checkstyle:linelength"})
    private void addMacraMeasureMaps() {
        addMacraMeasureMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "RT7 EP Stage 2", "Patient Care Record Exchange: Eligible Professional", "Required Test 7: Stage 2 Objective 5", false);
        addMacraMeasureMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "RT7 EP Stage 3", "Patient Care Record Exchange: Eligible Professional", "Required Test 7: Stage 3 Objective 7 Measure 1", false);
        addMacraMeasureMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "RT7 EC ACI Transition", "Patient Care Record Exchange: Eligible Clinician", "Required Test 7: Promoting Interoperability Transition Objective 6 Measure 1", false);
        addMacraMeasureMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "RT7 EC ACI", "Support Electronic Referral Loops by Sending Health Information (formerly Patient Care Record Exchange): Eligible Clinician", "Required Test 7: Promoting Interoperability Objective 5 Measure 1", false);
        addMacraMeasureMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "RT7 EH/CAH Stage 2", "Patient Care Record Exchange: Eligible Hospital/Critical Access Hospital", "Required Test 7: Stage 2 Objective 5", false);
        addMacraMeasureMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "RT7 EH/CAH Stage 3", "Support Electronic Referral Loops by Sending Health Information (formerly Patient Care Record Exchange):  Eligible Hospital/Critical Access Hospital", "Required Test 7: Stage 3 Objective 7 Measure 1", false);
        addMacraMeasureMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "RT8 EP Stage 3", "Request/Accept Patient Care Record: Eligible Professional", "Required Test 8: Stage 3 Objective 7 Measure 2", false);
        addMacraMeasureMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "RT8 EC ACI", "Request/Accept Patient Care Record: Eligible Clinician", "Required Test 8: Promoting Interoperability Objective 5 Measure 2", false);
        addMacraMeasureMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "RT8 EH/CAH Stage 3", "Request/Accept Patient Care Record: Eligible Hospital/Critical Access Hospital", "Required Test 8: Stage 3 Objective 7 Measure 2", false);
        addMacraMeasureMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "RT15 EH/CAH Stage 3", "Support Electronic Referral Loops by Receiving and Incorporating Health Information: Eligible Hospital/Critical Access Hospital", "Required Test 15: Stage 3", false);
        addMacraMeasureMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "RT15 EC", "Support Electronic Referral Loops by Receiving and Incorporating Health Information: Eligible Clinician", "Required Test 15: Promoting Interoperability", false);
        addMacraMeasureMap("170.315 (b)(2)", "Clinical Information Reconciliation and Incorporation (Cures Update)", "EC ACI Transition", "Medication/Clinical Information Reconciliation: Eligible Clinician", "Required Test 9: Promoting Interoperability Transition Objective 7 Measure 1", false);
        addMacraMeasureMap("170.315 (b)(2)", "Clinical Information Reconciliation and Incorporation (Cures Update)", "EC ACI", "Medication/Clinical Information Reconciliation: Eligible Clinician", "Required Test 9: Promoting Interoperability Objective 5 Measure 3", false);
        addMacraMeasureMap("170.315 (b)(2)", "Clinical Information Reconciliation and Incorporation (Cures Update)", "EH/CAH Stage 2", "Medication/Clinical Information Reconciliation: Eligible Hospital/Critical Access Hospital", "Required Test 9: Stage 2 Objective 7", false);
        addMacraMeasureMap("170.315 (b)(2)", "Clinical Information Reconciliation and Incorporation (Cures Update)", "EH/CAH Stage 3", "Medication/Clinical Information Reconciliation: Eligible Hospital/Critical Access Hospital", "Required Test 9: Stage 3 Objective 7 Measure 3", false);
        addMacraMeasureMap("170.315 (b)(2)", "Clinical Information Reconciliation and Incorporation (Cures Update)", "EP Stage 2", "Medication/Clinical Information Reconciliation: Eligible Professional", "Required Test 9: Stage 2 Objective 7", false);
        addMacraMeasureMap("170.315 (b)(2)", "Clinical Information Reconciliation and Incorporation (Cures Update)", "EP Stage 3", "Medication/Clinical Information Reconciliation: Eligible Professional", "Required Test 9: Stage 3 Objective 7 Measure 3", false);
        addMacraMeasureMap("170.315 (b)(2)", "Clinical Information Reconciliation and Incorporation (Cures Update)", "RT15 EH/CAH Stage 3", "Support Electronic Referral Loops by Receiving and Incorporating Health Information: Eligible Hospital/Critical Access Hospital", "Required Test 15: Stage 3", false);
        addMacraMeasureMap("170.315 (b)(2)", "Clinical Information Reconciliation and Incorporation (Cures Update)", "RT15 EC", "Support Electronic Referral Loops by Receiving and Incorporating Health Information: Eligible Clinician", "Required Test 15: Promoting Interoperability", false);
        addMacraMeasureMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "EP Stage 2", "Electronic Prescribing: Eligible Professional", "Required Test 1: Stage 2 Objective 4", false);
        addMacraMeasureMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "EP Stage 3", "Electronic Prescribing: Eligible Professional", "Required Test 1: Stage 3 Objective 2", false);
        addMacraMeasureMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "EC ACI Transition", "Electronic Prescribing: Eligible Clinician", "Required Test 1: Promoting Interoperability Transition Objective 2 Measure 1", false);
        addMacraMeasureMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "EC ACI", "Electronic Prescribing: Eligible Clinician", "Required Test 1: Promoting Interoperability Objective 2 Measure 1", false);
        addMacraMeasureMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "EH/CAH Stage 2", "Electronic Prescribing: Eligible Hospital/Critical Access Hospital", "Required Test 1: Stage 2 Objective 4", false);
        addMacraMeasureMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "EH/CAH Stage 3", "Electronic Prescribing: Eligible Hospital/Critical Access Hospital", "Required Test 1: Stage 3 Objective 2", false);
        addMacraMeasureMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "RT13 EH/CAH Stage 3", "Query of Prescription Drug Monitoring Program (PDMP): Eligible Hospital/Critical Access Hospital", "Required Test 13: Stage 3", true);
        addMacraMeasureMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "RT14 EH/CAH Stage 3", "Verify Opioid Treatment Agreement: Eligible Hospital/Critical Access Hospital", "Required Test 14: Stage 3", false);
        addMacraMeasureMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "RT13 EC", "Query of Prescription Drug Monitoring Program (PDMP): Eligible Clinician", "Required Test 13: Promoting Interoperability", true);
        addMacraMeasureMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "RT14 EC", "Verify Opioid Treatment Agreement: Eligible Clinician", "Required Test 14: Promoting Interoperability", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT2a EP Stage 2", "Patient Electronic Access: Eligible Professional", "Required Test 2: Stage 2 Objective 8 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT2a EP Stage 3", "Patient Electronic Access: Eligible Professional", "Required Test 2: Stage 3 Objective 5 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT2a EC ACI Transition", "Patient Electronic Access: Eligible Clinician", "Required Test 2: Promoting Interoperability Transition Objective 3 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT2a EC ACI", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Clinician", "Required Test 2: Promoting Interoperability Objective 3 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT2a EH/CAH Stage 2", "Patient Electronic Access: Eligible Hospital/Critical Access Hospital", "Required Test 2: Stage 2 Objective 8 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT2a EH/CAH Stage 3", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Hospital/Critical Access Hospital", "Required Test 2: Stage 3 Objective 5 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT2b EP Stage 2", "Patient Electronic Access: Eligible Professional", "Required Test 2: Stage 2 Objective 8 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT2b EP Stage 3", "Patient Electronic Access: Eligible Professional", "Required Test 2: Stage 3 Objective 5 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT2b EC ACI Transition", "Patient Electronic Access: Eligible Clinician", "Required Test 2: Promoting Interoperability Transition Objective 3 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT2b EC ACI", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Clinician", "Required Test 2: Promoting Interoperability Objective 3 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT2b EH/CAH Stage 2", "Patient Electronic Access: Eligible Hospital/Critical Access Hospital", "Required Test 2: Stage 2 Objective 8 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT2b EH/CAH Stage 3", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Hospital/Critical Access Hospital", "Required Test 2: Stage 3 Objective 5 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT4a EP Stage 2", "View, Download, or Transmit (VDT): Eligible Professional", "Required Test 4: Stage 2 Objective 8 Measure 2", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT4a EP Stage 3", "View, Download, or Transmit (VDT): Eligible Professional", "Required Test 4: Stage 3 Objective 6 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT4a EC ACI Transition", "View, Download, or Transmit (VDT): Eligible Clinician ", "Required Test 4: Promoting Interoperability Transition Objective 3 Measure 2", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT4a EC ACI", "View, Download, or Transmit (VDT): Eligible Clinician", "Required Test 4: Promoting Interoperability Objective 4 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT4a EH/CAH Stage 2", "View, Download, or Transmit (VDT): Eligible Hospital/Critical Access Hospital", "Required Test 4: Stage 2 Objective 8 Measure 2", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT4a EH/CAH Stage 3", "View, Download, or Transmit (VDT): Eligible Hospital/Critical Access Hospital", "Required Test 4: Stage 3 Objective 6 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT4b EP Stage 2", "View, Download, or Transmit (VDT): Eligible Professional", "Required Test 4: Stage 2 Objective 8 Measure 2", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT4b EP Stage 3", "View, Download, or Transmit (VDT): Eligible Professional", "Required Test 4: Stage 3 Objective 6 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT4b EC ACI Transition", "View, Download, or Transmit (VDT): Eligible Clinician", "Required Test 4: Promoting Interoperability Transition Objective 3 Measure 2", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT4b EC ACI", "View, Download, or Transmit (VDT): Eligible Clinician", "Required Test 4: Promoting Interoperability Objective 4 Measure 1", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT4b EH/CAH Stage 2", "View, Download, or Transmit (VDT): Eligible Hospital/Critical Access Hospital", "Required Test 4: Stage 2 Objective 8 Measure 2", false);
        addMacraMeasureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "RT4b EH/CAH Stage 3", "View, Download, or Transmit (VDT): Eligible Hospital/Critical Access Hospital", "Required Test 4: Stage 3 Objective 6 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT2a EP Stage 2", "Patient Electronic Access: Eligible Professional", "Required Test 2: Stage 2 Objective 8 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT2a EP Stage 3", "Patient Electronic Access: Eligible Professional", "Required Test 2: Stage 3 Objective 5 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT2a EC ACI Transition", "Patient Electronic Access: Eligible Clinician", "Required Test 2: Promoting Interoperability Transition Objective 3 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT2a EC ACI", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Clinician", "Required Test 2: Promoting Interoperability Objective 3 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT2a EH/CAH Stage 2", "Patient Electronic Access: Eligible Hospital/Critical Access Hospital", "Required Test 2: Stage 2 Objective 8 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT2a EH/CAH Stage 3", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Hospital/Critical Access Hospital", "Required Test 2: Stage 3 Objective 5 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT2c EP Stage 2", "Patient Electronic Access: Eligible Professional", "Required Test 2: Stage 2 Objective 8 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT2c EP Stage 3", "Patient Electronic Access: Eligible Professional", "Required Test 2: Stage 3 Objective 5 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT2c EC ACI Transition", "Patient Electronic Access: Eligible Clinician ", "Required Test 2: Promoting Interoperability Transition Objective 3 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT2c EC ACI", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Clinician", "Required Test 2: Promoting Interoperability Objective 3 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT2c EH/CAH Stage 2", "Patient Electronic Access: Eligible Hospital/Critical Access Hospital", "Required Test 2: Stage 2 Objective 8 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT2c EH/CAH Stage 3", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Hospital/Critical Access Hospital", "Required Test 4: Stage 3 Objective 6 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT4a EP Stage 2", "View, Download, or Transmit (VDT): Eligible Professional", "Required Test 4: Stage 2 Objective 8 Measure 2", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT4a EP Stage 3", "View, Download, or Transmit (VDT): Eligible Professional", "Required Test 4: Stage 3 Objective 6 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT4a EC ACI Transition", "View, Download, or Transmit (VDT): Eligible Clinician ", "Required Test 4: Promoting Interoperability Transition Objective 3 Measure 2", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT4a EC ACI", "View, Download, or Transmit (VDT): Eligible Clinician ", "Required Test 4: Promoting Interoperability Objective 4 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT4a EH/CAH Stage 2", "View, Download, or Transmit (VDT): Eligible Hospital/Critical Access Hospital", "Required Test 4: Stage 2 Objective 8 Measure 2", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT4a EH/CAH Stage 3", "View, Download, or Transmit (VDT): Eligible Hospital/Critical Access Hospital", "Required Test 4: Stage 3 Objective 6 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT4c EP Stage 2", "View, Download, or Transmit (VDT): Eligible Professional", "Required Test 4: Stage 2 Objective 8 Measure 2", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT4c EP Stage 3", "View, Download, or Transmit (VDT): Eligible Professional", "Required Test 4: Stage 3 Objective 6 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT4c EC ACI Transition", "View, Download, or Transmit (VDT): Eligible Clinician ", "Required Test 4: Promoting Interoperability Transition Objective 3 Measure 2", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT4c EC ACI", "View, Download, or Transmit (VDT): Eligible Clinician ", "Required Test 4: Promoting Interoperability Objective 4 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT4c EH/CAH Stage 2", "View, Download, or Transmit (VDT): Eligible Hospital/Critical Access Hospital", "Required Test 4: Stage 2 Objective 8 Measure 2", false);
        addMacraMeasureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "RT4c EH/CAH Stage 3", "View, Download, or Transmit (VDT): Eligible Hospital/Critical Access Hospital", "Required Test 4: Stage 3 Objective 6 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(10)", "Standardized API for Patient and Population Services", "RT2a EP Stage 3", "Patient Electronic Access: Eligible Professional", "Required Test 2: Stage 3 Objective 5 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(10)", "Standardized API for Patient and Population Services", "RT2a EH/CAH Stage 3", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Hospital/Critical Access Hospital", "Required Test 2: Stage 3 Objective 5 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(10)", "Standardized API for Patient and Population Services", "RT2a EC PI", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Clinician", "Required Test 2: Promoting Interoperability Objective 3 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(10)", "Standardized API for Patient and Population Services", "RT2c EP Stage 3", "Patient Electronic Access: Eligible Professional", "Required Test 2: Stage 3 Objective 5 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(10)", "Standardized API for Patient and Population Services", "RT2c EH/CAH Stage 3", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Hospital/Critical Access Hospital", "Required Test 2: Stage 3 Objective 5 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(10)", "Standardized API for Patient and Population Services", "RT2c EC PI", "Provide Patients Electronic Access to Their Health Information (formerly Patient Electronic Access): Eligible Clinician", "Required Test 2: Promoting Interoperability Objective 3 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(10)", "Standardized API for Patient and Population Services", "RT4a EP Stage 3", "View, Download, or Transmit (VDT): Eligible Professional", "Required Test 4: Stage 3 Objective 6 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(10)", "Standardized API for Patient and Population Services", "RT4a EH/CAH Stage 3", "View, Download, or Transmit (VDT): Eligible Hospital/Critical Access Hospital", "Required Test 4: Stage 3 Objective 6 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(10)", "Standardized API for Patient and Population Services", "RT4a EC PI", "View, Download, or Transmit (VDT):  Eligible Clinician", "Required Test 4: Promoting Interoperability Objective 4 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(10)", "Standardized API for Patient and Population Services", "RT4c EP Stage 3", "View, Download, or Transmit (VDT): Eligible Professional", "Required Test 4: Stage 3 Objective 6 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(10)", "Standardized API for Patient and Population Services", "RT4c EH/CAH Stage 3", "View, Download, or Transmit (VDT): Eligible Hospital/Critical Access Hospital", "Required Test 4: Stage 3 Objective 6 Measure 1", false);
        addMacraMeasureMap("170.315 (g)(10)", "Standardized API for Patient and Population Services", "RT4c EC PI", "View, Download, or Transmit (VDT):  Eligible Clinician", "Required Test 4: Promoting Interoperability Objective 4 Measure 1", false);
    }

    private void addMacraMeasureMap(String criterionNumber, String criterionTitle, String value, String name, String description,
            boolean removed) {
        if (!macraMeasureCriteriaMapExists(criterionNumber, criterionTitle, value)) {
            MacraMeasureDTO mm = new MacraMeasureDTO();
            CertificationCriterionDTO criterion = criterionDAO.getByNumberAndTitle(criterionNumber, criterionTitle);
            if (criterion == null) {
                LOGGER.error("Cannot insert macra measure for criteria that is not found: " + criterionNumber);
            } else {
                mm.setCriteriaId(criterion.getId());
                mm.setDescription(description);
                mm.setName(name);
                mm.setValue(value);
                mm.setRemoved(removed);
                insertableMmDao.create(mm);
                LOGGER.info("Inserted macra measure " + value + " for criterion " + criterion.getNumber() + ":" + criterionTitle);
            }
        } else {
            LOGGER.info("Mapping from " + criterionNumber + ":" + criterionTitle
                    + " to macra measure " + value + " already exists.");
        }
    }

    private boolean macraMeasureCriteriaMapExists(String criterionNumber, String criterionTitle, String value) {
        MacraMeasureDTO mm = insertableMmDao.getByCriteriaNumberTitleAndValue(criterionNumber, criterionTitle, value);
        return mm != null;
    }

    @SuppressWarnings({"checkstyle:linelength"})
    private void addTestDataMaps() {
        addTestDataMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "ONC Test Method");
        addTestDataMap("170.315 (b)(2)", "Clinical Information Reconciliation and Incorporation (Cures Update)", "ONC Test Method");
        addTestDataMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "ONC Test Method");
        addTestDataMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "Not Applicable");
        addTestDataMap("170.315 (b)(7)", "Security Tags - Summary of Care - Send (Cures Update)", "ONC Test Method");
        addTestDataMap("170.315 (b)(8)", "Security Tags - Summary of Care - Receive (Cures Update)", "ONC Test Method");
        addTestDataMap("170.315 (b)(9)", "Care Plan (Cures Update)", "ONC Test Method");
        addTestDataMap("170.315 (b)(10)", "Electronic Health Information Export", "ONC Test Method");
        addTestDataMap("170.315 (c)(3)", "Clinical Quality Measures - Report (Cures Update)", "ONC Test Method");
        addTestDataMap("170.315 (c)(3)", "Clinical Quality Measures - Report (Cures Update)", "NCQA eCQM Test Method");
        addTestDataMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "ONC Test Method");
        addTestDataMap("170.315 (g)(6)", "Consolidated CDA Creation Performance (Cures Update)", "ONC Test Method");
        addTestDataMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "ONC Test Method");
        addTestDataMap("170.315 (g)(10)", "Standardized API for Patient and Population Services", "ONC Test Method");
    }

    private void addTestDataMap(String criterionNumber, String criterionTitle, String testDataName) {
        if (!testDataMapExists(criterionNumber, criterionTitle, testDataName)) {
            TestDataDTO testData = insertableTestDataDao.getTestDataByName(testDataName);
            CertificationCriterionDTO criterion = criterionDAO.getByNumberAndTitle(criterionNumber, criterionTitle);
            if (testData == null) {
                LOGGER.error("Could not find test data " + testDataName);
            }
            if (criterion == null) {
                LOGGER.error("Could not find criterion " + criterionNumber + ":" + criterionTitle);
            }
            if (testData != null && criterion != null) {
                insertableTestDataDao.create(testData, criterion);
                LOGGER.info("Added test data mapping from " + criterionNumber + ":" + criterionTitle + " to " + testDataName);
            }
        } else {
            LOGGER.info("Test data mapping from " + criterionNumber + ":"
                    + criterionTitle + " to " + testDataName + " already exists.");
        }
    }

    private boolean testDataMapExists(String criterionNumber, String criterionTitle, String testDataName) {
        TestDataDTO td = insertableTestDataDao.getByCriteriaNumberTitleAndValue(criterionNumber, criterionTitle, testDataName);
        return td != null;
    }

    @SuppressWarnings({"checkstyle:linelength"})
    private void addTestProcedureMaps() {
        addTestProcedureMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "ONC Test Method");
        addTestProcedureMap("170.315 (b)(2)", "Clinical Information Reconciliation and Incorporation (Cures Update)", "ONC Test Method");
        addTestProcedureMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "ONC Test Method");
        addTestProcedureMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "ONC Test Method - Surescripts (Alternative)");
        addTestProcedureMap("170.315 (b)(7)", "Security Tags - Summary of Care - Send (Cures Update)", "ONC Test Method");
        addTestProcedureMap("170.315 (b)(8)", "Security Tags - Summary of Care - Receive (Cures Update)", "ONC Test Method");
        addTestProcedureMap("170.315 (b)(9)", "Care Plan (Cures Update)", "ONC Test Method");
        addTestProcedureMap("170.315 (b)(10)", "Electronic Health Information Export", "ONC Test Method");
        addTestProcedureMap("170.315 (c)(3)", "Clinical Quality Measures - Report (Cures Update)", "ONC Test Method");
        addTestProcedureMap("170.315 (c)(3)", "Clinical Quality Measures - Report (Cures Update)", "NCQA eCQM Test Method");
        addTestProcedureMap("170.315 (d)(2)", "Auditable Events and Tamper-Resistance (Cures Update)", "ONC Test Method");
        addTestProcedureMap("170.315 (d)(3)", "Audit Report(s) (Cures Update)", "ONC Test Method");
        addTestProcedureMap("170.315 (d)(10)", "Auditing Actions on Health Information (Cures Update)", "ONC Test Method");
        addTestProcedureMap("170.315 (d)(12)", "Encrypt Authentication Credentials", "ONC Test Method");
        addTestProcedureMap("170.315 (d)(13)", "Multi-Factor Authentication", "ONC Test Method");
        addTestProcedureMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "ONC Test Method");
        addTestProcedureMap("170.315 (f)(5)", "Transmission to Public Health Agencies - Electronic Case Reporting (Cures Update)", "ONC Test Method");
        addTestProcedureMap("170.315 (g)(6)", "Consolidated CDA Creation Performance (Cures Update)", "ONC Test Method");
        addTestProcedureMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "ONC Test Method");
        addTestProcedureMap("170.315 (g)(10)", "Standardized API for Patient and Population Services", "ONC Test Method");
    }

    private void addTestProcedureMap(String criterionNumber, String criterionTitle, String testProcedureName) {
        if (!testProcedureMapExists(criterionNumber, criterionTitle, testProcedureName)) {
            TestProcedureDTO testProc = insertableTestProcDao.getTestProcedureByName(testProcedureName);
            CertificationCriterionDTO criterion = criterionDAO.getByNumberAndTitle(criterionNumber, criterionTitle);
            if (testProc == null) {
                LOGGER.error("Could not find test procedure " + testProcedureName);
            }
            if (criterion == null) {
                LOGGER.error("Could not find criterion " + criterionNumber + ":" + criterionTitle);
            }
            if (testProc != null && criterion != null) {
                insertableTestProcDao.create(testProc, criterion);
                LOGGER.info("Added test procedure mapping from " + criterionNumber
                        + ":" + criterionTitle + " to " + testProcedureName);
            }
        } else {
            LOGGER.info("Test procedure mapping from " + criterionNumber
                    + ":" + criterionTitle + " to " + testProcedureName + " already exists.");
        }
    }

    private boolean testProcedureMapExists(String criterionNumber, String criterionTitle, String testProcedureName) {
        TestProcedureDTO tp = insertableTestProcDao.getByCriteriaNumberTitleAndValue(
                criterionNumber, criterionTitle, testProcedureName);
        return tp != null;
    }

    @SuppressWarnings({"checkstyle:linelength"})
    private void addTestFunctionalities() {
        addTestFunctionality("(b)(3)(ii)(B)(1)", "Create and respond to new prescriptions (NewRxRequest, NewRxResponseDenied)");
        addTestFunctionality("(b)(3)(ii)(B)(2)", "Receive fill status notifications (RxFillIndicator)");
        addTestFunctionality("(b)(3)(ii)(B)(3)", "Ask the Mailbox if there are any transactions (GetMessage)");
        addTestFunctionality("(b)(3)(ii)(B)(4)", "Request to send an additional supply of medication (Resupply)");
        addTestFunctionality("(b)(3)(ii)(B)(5)", "Communicate drug administration events (DrugAdministration)");
        addTestFunctionality("(b)(3)(ii)(B)(6)", "Request and respond to transfer one or more prescriptions between pharmacies (RxTransferRequest, RxTransferResponse, RxTransferConfirm)");
        addTestFunctionality("(b)(3)(ii)(B)(7)", "Recertify the continued administration of a medication order (Recertification)");
        addTestFunctionality("(b)(3)(ii)(B)(8)", "Complete Risk Evaluation and Mitigation Strategy (REMS) transactions (REMSInitiationRequest, REMSInitiationResponse, REMSRequest, and REMSResponse)");
        addTestFunctionality("(b)(3)(ii)(B)(9)", "Electronic prior authorization transactions (PAInitiationRequest, PAInitiationResponse, PARequest, PAResponse, PAAppealRequest, PAAppealResponse,  PACancelRequest, and PACancelResponse).");
        addTestFunctionality("(b)(3)(ii)(D)", "Optional: 170.315(b)(3)(ii)(D) For each transaction listed in paragraph (b)(3)(ii)(C) of this section, the technology must be able to receive and transmit the reason for the prescription using the <IndicationforUse> element in the SIG segment");
    }

    @SuppressWarnings({"checkstyle:linelength"})
    private void addTestFunctionalityMaps() {
        addTestFunctionalityMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "(b)(1)(ii)(A)(5)(i)");
        addTestFunctionalityMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "(b)(1)(ii)(A)(5)(ii)");
        addTestFunctionalityMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "(b)(1)(iii)(E)");
        addTestFunctionalityMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "(b)(1)(iii)(F)");
        addTestFunctionalityMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "(b)(1)(iii)(G)(1)(ii)");
        addTestFunctionalityMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "170.102(13)(ii)(C)");
        addTestFunctionalityMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "170.102(19)(i)");
        addTestFunctionalityMap("170.315 (b)(1)", "Transitions of Care (Cures Update)", "170.102(19)(ii)");

        addTestFunctionalityMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "(b)(3)(ii)(B)(1)");
        addTestFunctionalityMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "(b)(3)(ii)(B)(2)");
        addTestFunctionalityMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "(b)(3)(ii)(B)(3)");
        addTestFunctionalityMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "(b)(3)(ii)(B)(4)");
        addTestFunctionalityMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "(b)(3)(ii)(B)(5)");
        addTestFunctionalityMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "(b)(3)(ii)(B)(6)");
        addTestFunctionalityMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "(b)(3)(ii)(B)(7)");
        addTestFunctionalityMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "(b)(3)(ii)(B)(8)");
        addTestFunctionalityMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "(b)(3)(ii)(B)(9)");
        addTestFunctionalityMap("170.315 (b)(3)", "Electronic Prescribing (Cures Update)", "(b)(3)(ii)(D)");

        addTestFunctionalityMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "(e)(1)(i)(A)(2)");
        addTestFunctionalityMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "(e)(1)(i)(A)(3)");
        addTestFunctionalityMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "(e)(1)(i)(B)(2)(i)");
        addTestFunctionalityMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "(e)(1)(i)(B)(2)(ii)");
        addTestFunctionalityMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "(e)(1)(i)(B)(3)");
        addTestFunctionalityMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "(e)(1)(i)(C)(2)");
        addTestFunctionalityMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "170.102(13)(ii)(C)");
        addTestFunctionalityMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "170.102(19)(i)");
        addTestFunctionalityMap("170.315 (e)(1)", "View, Download, and Transmit to 3rd Party (Cures Update)", "170.102(19)(ii)");

        addTestFunctionalityMap("170.315 (f)(5)", "Transmission to Public Health Agencies - Electronic Case Reporting (Cures Update)", "170.102(13)(ii)(C)");
        addTestFunctionalityMap("170.315 (f)(5)", "Transmission to Public Health Agencies - Electronic Case Reporting (Cures Update)", "170.102(19)(i)");
        addTestFunctionalityMap("170.315 (f)(5)", "Transmission to Public Health Agencies - Electronic Case Reporting (Cures Update)", "170.102(19)(ii)");

        addTestFunctionalityMap("170.315 (g)(6)", "Consolidated CDA Creation Performance (Cures Update)", "170.102(13)(ii)(C)");
        addTestFunctionalityMap("170.315 (g)(6)", "Consolidated CDA Creation Performance (Cures Update)", "170.102(19)(i)");
        addTestFunctionalityMap("170.315 (g)(6)", "Consolidated CDA Creation Performance (Cures Update)", "170.102(19)(ii)");

        addTestFunctionalityMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "170.102(13)(ii)(C)");
        addTestFunctionalityMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "170.102(19)(i)");
        addTestFunctionalityMap("170.315 (g)(9)", "Application Access - All Data Request (Cures Update)", "170.102(19)(ii)");
    }

    private void addTestFunctionality(String number, String title) {
        if (!testFunctionalityExists(number)) {
            insertableTestFuncDao.create(number, title);
            LOGGER.info("Added test functionality for number " + number
                    + " with title: " + title);
        } else {
            LOGGER.info("Test functionality for number " + number
                    + " with title: " + title + " already exists");
        }
    }

    private void addTestFunctionalityMap(String criterionNumber, String criterionTitle, String testFuncNumber) {
        if (!testFunctionalityMapExists(criterionNumber, criterionTitle, testFuncNumber)) {
            TestFunctionalityDTO testFunc = insertableTestFuncDao.getTestFunctionalityByNumberAndEdition(testFuncNumber,
                    CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
            CertificationCriterionDTO criterion = criterionDAO.getByNumberAndTitle(criterionNumber, criterionTitle);
            if (testFunc == null) {
                LOGGER.error("Could not find test functionality " + testFuncNumber);
            }
            if (criterion == null) {
                LOGGER.error("Could not find criterion " + criterionNumber + ":" + criterionTitle);
            }
            if (testFunc != null && criterion != null) {
                insertableTestFuncDao.createMapping(testFunc, criterion);
                LOGGER.info("Added test functionality mapping from " + criterionNumber
                        + ":" + criterionTitle + " to " + testFunc.getNumber());
            }
        } else {
            LOGGER.info("Test functionality mapping from " + criterionNumber
                    + ":" + criterionTitle + " to " + testFuncNumber + " already exists.");
        }
    }

    private boolean testFunctionalityExists(String number) {
        TestFunctionalityDTO dto = testFunctionalityDAO.getByNumberAndEdition(number, EDITION_2015_ID);
        return dto != null;
    }

    private boolean testFunctionalityMapExists(String criterionNumber, String criterionTitle, String testFuncNumber) {
        TestFunctionalityCriteriaMapDTO tfMap = insertableTestFuncDao.getByCriteriaNumberTitleAndValue(criterionNumber,
                criterionTitle, testFuncNumber);
        return tfMap != null;
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
                .collect(Collectors.toList());
    }

    @Transactional
    private List<Long> getPendingListingIds() {
        return pendingCertifiedProductDAO.getAllIds();
    }

    private void setSecurityContext() {

        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(ADMIN_ID);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Component("insertableMacraMeasureDao")
    private static class InsertableMacraMeasureDao extends BaseDAOImpl {

        @SuppressWarnings("unused")
        InsertableMacraMeasureDao() {
            super();
        }

        @Transactional
        public MacraMeasureDTO getByCriteriaNumberTitleAndValue(String criteriaNumber, String criteriaTitle, String value) {
            Query query = entityManager.createQuery(
                    "FROM MacraMeasureEntity mme "
                            + "LEFT OUTER JOIN FETCH mme.certificationCriterion cce "
                            + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                            + "WHERE (NOT mme.deleted = true) "
                            + "AND (UPPER(cce.number) = :criteriaNumber) "
                            + "AND cce.title = :criteriaTitle "
                            + "AND (UPPER(mme.value) = :value)",
                    MacraMeasureEntity.class);
            query.setParameter("criteriaNumber", criteriaNumber.trim().toUpperCase());
            query.setParameter("criteriaTitle", criteriaTitle);
            query.setParameter("value", value.trim().toUpperCase());
            @SuppressWarnings("unchecked") List<MacraMeasureEntity> result = query.getResultList();
            if (result == null || result.size() == 0) {
                return null;
            }
            return new MacraMeasureDTO(result.get(0));
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
            toInsert.setRemoved(dto.getRemoved());
            toInsert.setValue(dto.getValue());
            super.create(toInsert);
        }
    }

    @Component("insertableTestDataDao")
    private static class InsertableTestDataDao extends BaseDAOImpl {

        @SuppressWarnings("unused")
        InsertableTestDataDao() {
            super();
        }

        @Transactional
        public TestDataDTO getByCriteriaNumberTitleAndValue(String criteriaNumber, String criteriaTitle, String value) {
            Query query = entityManager.createQuery("SELECT tdMap "
                    + "FROM TestDataCriteriaMapEntity tdMap "
                    + "JOIN FETCH tdMap.testData td "
                    + "JOIN FETCH tdMap.certificationCriterion cce "
                    + "JOIN FETCH cce.certificationEdition "
                    + "WHERE tdMap.deleted <> true "
                    + "AND td.deleted <> true "
                    + "AND (UPPER(cce.number) = :criteriaNumber) "
                    + "AND cce.title = :criteriaTitle "
                    + "AND (UPPER(td.name) = :value)",
                    TestDataCriteriaMapEntity.class);
            query.setParameter("criteriaNumber", criteriaNumber.trim().toUpperCase());
            query.setParameter("criteriaTitle", criteriaTitle);
            query.setParameter("value", value.trim().toUpperCase());

            @SuppressWarnings("unchecked") List<TestDataCriteriaMapEntity> results = query.getResultList();
            if (results == null || results.size() == 0) {
                return null;
            }
            List<TestDataEntity> tds = new ArrayList<TestDataEntity>();
            for (TestDataCriteriaMapEntity result : results) {
                tds.add(result.getTestData());
            }
            return new TestDataDTO(tds.get(0));
        }

        @Transactional
        public TestDataDTO getTestDataByName(String name) {
            String hql = "SELECT td "
                    + "FROM TestDataEntity td "
                    + "WHERE td.name = :name "
                    + "AND deleted = false";
            Query query = entityManager.createQuery(hql);
            query.setParameter("name", name);
            @SuppressWarnings("unchecked") List<TestDataEntity> tdEntities = query.getResultList();
            TestDataDTO result = null;
            if (tdEntities != null && tdEntities.size() > 0) {
                result = new TestDataDTO(tdEntities.get(0));
            }
            return result;
        }

        @Transactional
        public void create(TestDataDTO testDataDto, CertificationCriterionDTO criterion) {
            TestDataCriteriaMapEntity toInsert = new TestDataCriteriaMapEntity();
            toInsert.setCertificationCriterionId(criterion.getId());
            toInsert.setCreationDate(new Date());
            toInsert.setDeleted(false);
            toInsert.setLastModifiedDate(new Date());
            toInsert.setLastModifiedUser(AuthUtil.getAuditId());
            toInsert.setTestDataId(testDataDto.getId());
            super.create(toInsert);
        }
    }

    @Component("insertableTestProcedureDao")
    private static class InsertableTestProcedureDao extends BaseDAOImpl {

        @SuppressWarnings("unused")
        InsertableTestProcedureDao() {
            super();
        }

        @Transactional
        public TestProcedureDTO getByCriteriaNumberTitleAndValue(String criteriaNumber, String criteriaTitle, String value) {
            Query query = entityManager.createQuery("SELECT tpMap "
                    + "FROM TestProcedureCriteriaMapEntity tpMap "
                    + "JOIN FETCH tpMap.testProcedure tp "
                    + "JOIN FETCH tpMap.certificationCriterion cce "
                    + "JOIN FETCH cce.certificationEdition "
                    + "WHERE tpMap.deleted <> true "
                    + "AND tp.deleted <> true "
                    + "AND (UPPER(cce.number) = :criteriaNumber) "
                    + "AND cce.title = :criteriaTitle "
                    + "AND (UPPER(tp.name) = :value)",
                    TestProcedureCriteriaMapEntity.class);
            query.setParameter("criteriaNumber", criteriaNumber.trim().toUpperCase());
            query.setParameter("criteriaTitle", criteriaTitle);
            query.setParameter("value", value.trim().toUpperCase());

            @SuppressWarnings("unchecked") List<TestProcedureCriteriaMapEntity> results = query.getResultList();
            if (results == null || results.size() == 0) {
                return null;
            }
            List<TestProcedureEntity> tps = new ArrayList<TestProcedureEntity>();
            for (TestProcedureCriteriaMapEntity result : results) {
                tps.add(result.getTestProcedure());
            }
            return new TestProcedureDTO(tps.get(0));
        }

        @Transactional
        public TestProcedureDTO getTestProcedureByName(String name) {
            String hql = "SELECT tp "
                    + "FROM TestProcedureEntity tp "
                    + "WHERE tp.name = :name "
                    + "AND deleted = false";
            Query query = entityManager.createQuery(hql);
            query.setParameter("name", name);
            @SuppressWarnings("unchecked") List<TestProcedureEntity> tpEntities = query.getResultList();
            TestProcedureDTO result = null;
            if (tpEntities != null && tpEntities.size() > 0) {
                result = new TestProcedureDTO(tpEntities.get(0));
            }
            return result;
        }

        @Transactional
        public void create(TestProcedureDTO testProcDto, CertificationCriterionDTO criterion) {
            TestProcedureCriteriaMapEntity toInsert = new TestProcedureCriteriaMapEntity();
            toInsert.setCertificationCriterionId(criterion.getId());
            toInsert.setCreationDate(new Date());
            toInsert.setDeleted(false);
            toInsert.setLastModifiedDate(new Date());
            toInsert.setLastModifiedUser(AuthUtil.getAuditId());
            toInsert.setTestProcedureId(testProcDto.getId());
            super.create(toInsert);
        }
    }

    @Component("insertableTestFunctionalityDao")
    private static class InsertableTestFunctionalityDao extends BaseDAOImpl {

        @Autowired
        private CertificationEditionDAO editionDAO;

        @SuppressWarnings("unused")
        InsertableTestFunctionalityDao() {
            super();
        }

        @Transactional
        public TestFunctionalityCriteriaMapDTO getByCriteriaNumberTitleAndValue(String criteriaNumber,
                String criteriaTitle, String value) {
            Query query = entityManager.createQuery("SELECT tfMap "
                    + "FROM TestFunctionalityCriteriaMapEntity tfMap "
                    + "JOIN FETCH tfMap.testFunctionality tf "
                    + "JOIN FETCH tfMap.criteria cce "
                    + "JOIN FETCH cce.certificationEdition "
                    + "WHERE tfMap.deleted <> true "
                    + "AND tf.deleted <> true "
                    + "AND (UPPER(cce.number) = :criteriaNumber) "
                    + "AND cce.title = :criteriaTitle "
                    + "AND (UPPER(tf.number) = :value)",
                    TestFunctionalityCriteriaMapEntity.class);
            query.setParameter("criteriaNumber", criteriaNumber.trim().toUpperCase());
            query.setParameter("criteriaTitle", criteriaTitle);
            query.setParameter("value", value.trim().toUpperCase());

            @SuppressWarnings("unchecked") List<TestFunctionalityCriteriaMapEntity> results = query.getResultList();
            if (results == null || results.size() == 0) {
                return null;
            }
            List<TestFunctionalityCriteriaMapDTO> existingDtos = new ArrayList<TestFunctionalityCriteriaMapDTO>();
            for (TestFunctionalityCriteriaMapEntity result : results) {
                existingDtos.add(new TestFunctionalityCriteriaMapDTO(result));
            }
            return existingDtos.get(0);
        }

        @Transactional
        public TestFunctionalityDTO getTestFunctionalityByNumberAndEdition(String number, String year) {
            String hql = "SELECT tf "
                    + "FROM TestFunctionalityEntity tf "
                    + "JOIN FETCH tf.certificationEdition ce "
                    + "WHERE tf.number = :number "
                    + "AND ce.year = :year "
                    + "AND tf.deleted = false";
            Query query = entityManager.createQuery(hql);
            query.setParameter("number", number);
            query.setParameter("year", year);
            @SuppressWarnings("unchecked") List<TestFunctionalityEntity> tfEntities = query.getResultList();
            TestFunctionalityDTO result = null;
            if (tfEntities != null && tfEntities.size() > 0) {
                result = new TestFunctionalityDTO(tfEntities.get(0));
            }
            return result;
        }

        @Transactional
        public void createMapping(TestFunctionalityDTO testFuncDto, CertificationCriterionDTO criterion) {
            TestFunctionalityCriteriaMapEntity toInsert = new TestFunctionalityCriteriaMapEntity();
            toInsert.setCertificationCriterionId(criterion.getId());
            toInsert.setCreationDate(new Date());
            toInsert.setDeleted(false);
            toInsert.setLastModifiedDate(new Date());
            toInsert.setLastModifiedUser(AuthUtil.getAuditId());
            toInsert.setTestFunctionalityId(testFuncDto.getId());
            super.create(toInsert);
        }

        @Transactional
        public void create(String number, String name) {
            TestFunctionalityEntity toCreate = new TestFunctionalityEntity();
            CertificationEditionEntity edition = new CertificationEditionEntity(editionDAO.getByYear(EDITION_2015).getId());

            toCreate.setNumber(number);
            toCreate.setName(name);
            toCreate.setCertificationEdition(edition);
            toCreate.setCertificationEditionId(edition.getId());
            toCreate.setCreationDate(new Date());
            toCreate.setDeleted(false);
            toCreate.setLastModifiedDate(new Date());
            toCreate.setLastModifiedUser(AuthUtil.getAuditId());
            super.create(toCreate);
        }
    }

    @Component("pendingCertifiedProductDaoIdsOnly")
    private static class PendingCertifiedProductDaoIdsOnly extends BaseDAOImpl {

        @SuppressWarnings("unused")
        PendingCertifiedProductDaoIdsOnly() {
            super();
        }

        @Transactional
        public List<Long> getAllIds() {
            List<PendingCertifiedProductEntity> entities = entityManager.createQuery(
                    "SELECT pcp from PendingCertifiedProductEntity pcp "
                            + "WHERE pcp.certificationEdition = '2015' "
                            + "AND pcp.deleted = false",
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
