package gov.healthit.chpl.surveillance.report;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.scheduler.job.surveillanceReport.AnnualReportGenerationJob;
import gov.healthit.chpl.scheduler.job.surveillanceReport.QuarterlyReportGenerationJob;
import gov.healthit.chpl.surveillance.report.domain.AnnualReport;
import gov.healthit.chpl.surveillance.report.domain.PrivilegedSurveillance;
import gov.healthit.chpl.surveillance.report.domain.Quarter;
import gov.healthit.chpl.surveillance.report.domain.QuarterlyReport;
import gov.healthit.chpl.surveillance.report.domain.RelevantListing;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceOutcome;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceProcessType;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class SurveillanceReportManager extends SecuredManager {

    private ActivityManager activityManager;
    private UserManager userManager;
    private SchedulerManager schedulerManager;
    private QuarterlyReportDAO quarterlyDao;
    private PrivilegedSurveillanceDAO quarterlySurvMapDao;
    private AnnualReportDAO annualDao;
    private QuarterDAO quarterDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public SurveillanceReportManager(ActivityManager activityManager,
            UserManager userManager,
            SchedulerManager schedulerManager,
            QuarterlyReportDAO quarterlyDao,
            PrivilegedSurveillanceDAO quarterlySurvMapDao,
            AnnualReportDAO annualDao, QuarterDAO quarterDao,
            ErrorMessageUtil msgUtil) {
        this.activityManager = activityManager;
        this.userManager = userManager;
        this.schedulerManager = schedulerManager;
        this.quarterlyDao = quarterlyDao;
        this.quarterlySurvMapDao = quarterlySurvMapDao;
        this.annualDao = annualDao;
        this.quarterDao = quarterDao;
        this.msgUtil = msgUtil;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY)")
    public Set<KeyValueModel> getSurveillanceOutcomes() {
        List<SurveillanceOutcome> outcomes = quarterlySurvMapDao.getSurveillanceOutcomes();
        Set<KeyValueModel> result = new HashSet<KeyValueModel>();
        for (SurveillanceOutcome outcome : outcomes) {
            KeyValueModel currOutcome = new KeyValueModel();
            currOutcome.setId(outcome.getId());
            currOutcome.setName(outcome.getName());
            result.add(currOutcome);
        }
        return result;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY)")
    public Set<KeyValueModel> getSurveillanceProcessTypes() {
        List<SurveillanceProcessType> pts = quarterlySurvMapDao.getSurveillanceProcessTypes();
        Set<KeyValueModel> result = new HashSet<KeyValueModel>();
        for (SurveillanceProcessType pt : pts) {
            KeyValueModel currProcessType = new KeyValueModel();
            currProcessType.setId(pt.getId());
            currProcessType.setName(pt.getName());
            result.add(currProcessType);
        }
        return result;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).CREATE_ANNUAL, #toCreate)")
    public AnnualReport createAnnualReport(AnnualReport toCreate)
    throws EntityCreationException, InvalidArgumentsException, JsonProcessingException, EntityRetrievalException {
        //Annual report has to be associated with a year and an ACB

        if (toCreate == null || toCreate.getYear() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingYear"));
        } else if (toCreate.getAcb() == null || toCreate.getAcb().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingAcb"));
        }

        //make sure there's not already an annual report for this acb and year
        AnnualReport existingAnnualReport =
                annualDao.getByAcbAndYear(toCreate.getAcb().getId(), toCreate.getYear());
        if (existingAnnualReport != null) {
            throw new EntityCreationException(msgUtil.getMessage("report.annualSurveillance.exists"));
        }

        AnnualReport created = annualDao.create(toCreate);
        AnnualReport afterAnnualReport = annualDao.getById(created.getId());
        activityManager.addActivity(ActivityConcept.ANNUAL_REPORT, created.getId(),
                "Created annual report.", null, afterAnnualReport);
        return created;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).UPDATE_ANNUAL, #toUpdate)")
    public AnnualReport updateAnnualReport(AnnualReport toUpdate)
    throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        AnnualReport before = annualDao.getById(toUpdate.getId());
        AnnualReport updated = annualDao.update(toUpdate);
        activityManager.addActivity(ActivityConcept.ANNUAL_REPORT, updated.getId(),
                "Updated annual report.", before, updated);
        return updated;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).DELETE_ANNUAL, #id)")
    public void deleteAnnualReport(Long id)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        AnnualReport before = annualDao.getById(id);
        annualDao.delete(id);
        activityManager.addActivity(ActivityConcept.ANNUAL_REPORT, id,
                "Deleted annual report.", before, null);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_ANNUAL)")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_ANNUAL, filterObject)")
    public List<AnnualReport> getAnnualReports() {
        return annualDao.getAll();
    }

    @Transactional
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_ANNUAL,"
            + "returnObject)")
    public AnnualReport getAnnualReport(Long id) throws EntityRetrievalException {
        return annualDao.getById(id);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).EXPORT_ANNUAL, "
            + "#annualReportId)")
    public ChplOneTimeTrigger exportAnnualReportAsBackgroundJob(Long annualReportId)
            throws ValidationException, SchedulerException, UserRetrievalException {
        UserDTO jobUser = null;
        try {
            jobUser = userManager.getById(AuthUtil.getCurrentUser().getId());
        } catch (UserRetrievalException ex) {
            LOGGER.error("Could not find user to execute job.");
        }

        ChplOneTimeTrigger exportAnnualReportTrigger = new ChplOneTimeTrigger();
        ChplJob expoertAnnualReportJob = new ChplJob();
        expoertAnnualReportJob.setName(AnnualReportGenerationJob.JOB_NAME);
        expoertAnnualReportJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(AnnualReportGenerationJob.ANNUAL_REPORT_ID_KEY, annualReportId);
        jobDataMap.put(AnnualReportGenerationJob.USER_KEY, jobUser);
        expoertAnnualReportJob.setJobDataMap(jobDataMap);
        exportAnnualReportTrigger.setJob(expoertAnnualReportJob);
        exportAnnualReportTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.FIVE_SECONDS_IN_MILLIS);
        exportAnnualReportTrigger = schedulerManager.createBackgroundJobTrigger(exportAnnualReportTrigger);

        return exportAnnualReportTrigger;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).CREATE_QUARTERLY, #createRequest)")
    public Long createQuarterlyReport(QuarterlyReport createRequest)
    throws EntityCreationException, InvalidArgumentsException, JsonProcessingException, EntityRetrievalException, ValidationException {

        reviewQuarterlyReportForDeprecatedFields(createRequest);
        reviewQuarterlyReportToCreate(createRequest);
        Long createdReportId = quarterlyDao.create(createRequest);
        copyPreviousReportDataIntoNextReport(createRequest);
        QuarterlyReport afterQuarterlyReport = quarterlyDao.getById(createdReportId);
        activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT, createdReportId,
                "Created quarterly report.", null, afterQuarterlyReport);
        return createdReportId;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).UPDATE_QUARTERLY, #updateRequest)")
    public void updateQuarterlyReport(QuarterlyReport updateRequest)
    throws EntityRetrievalException, JsonProcessingException, EntityCreationException, ValidationException {
        QuarterlyReport origReport = quarterlyDao.getById(updateRequest.getId());
        reviewQuarterlyReportForDeprecatedFields(updateRequest, origReport);
        //above line throws entity retrieval exception if bad id
        quarterlyDao.update(updateRequest);
        QuarterlyReport updatedReport = quarterlyDao.getById(updateRequest.getId());
        activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT, updateRequest.getId(),
                "Updated quarterly report.", origReport, updatedReport);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).UPDATE_QUARTERLY, #toUpdate.quarterlyReport)")
    public PrivilegedSurveillance createOrUpdateQuarterlyReportSurveillanceMap(PrivilegedSurveillance toUpdate)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        // make sure passed-in surveillance is relevant to the report i.e. that it was open at some point
        //during the reporting period
        if (!quarterlyDao.isSurveillanceRelevant(toUpdate.getQuarterlyReport(), toUpdate.getId())) {
            throw new EntityCreationException(
                    msgUtil.getMessage("report.quarterlySurveillance.surveillance.notRelevant",
                            toUpdate.getFriendlyId(), toUpdate.getQuarterlyReport().getQuarter()));
        }
        PrivilegedSurveillance existing = quarterlySurvMapDao.getByReportAndSurveillance(
                toUpdate.getQuarterlyReport().getId(),
                toUpdate.getId());

        RelevantListing beforeRelevantListing = getRelevantListing(toUpdate.getQuarterlyReport(), toUpdate.getCertifiedProductId());
        PrivilegedSurveillance result = null;
        if (existing == null) {
             quarterlySurvMapDao.create(toUpdate.getQuarterlyReport().getId(), toUpdate);
        } else {
            toUpdate.setMappingId(existing.getMappingId());
             quarterlySurvMapDao.update(toUpdate.getQuarterlyReport().getId(), toUpdate.getId(), toUpdate);
        }
        RelevantListing afterRelevantListing = getRelevantListing(toUpdate.getQuarterlyReport(), toUpdate.getCertifiedProductId());
        activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT_LISTING, toUpdate.getQuarterlyReport().getId(),
                "Updated relevant listing for quarterly report.", beforeRelevantListing, afterRelevantListing);
        return result;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).DELETE_QUARTERLY, #id)")
    public void deleteQuarterlyReport(Long id)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        QuarterlyReport report = quarterlyDao.getById(id);
        quarterlyDao.delete(id);
        activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT, id,
                "Deleted quarterly report.", report, null);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY)")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY, filterObject)")
    public List<QuarterlyReport> getQuarterlyReports() {
        List<QuarterlyReport> reports = quarterlyDao.getAll();
        return reports;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY)")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY, filterObject)")
    public List<QuarterlyReport> getQuarterlyReports(Long acbId, Integer year) {
        List<QuarterlyReport> reports = quarterlyDao.getByAcbAndYear(acbId, year);
        return reports;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "#report)")
    public RelevantListing getRelevantListing(QuarterlyReport report, Long listingId) {
        RelevantListing relevantListing = quarterlyDao.getRelevantListing(listingId, report);
        if (relevantListing != null) {
            relevantListing.setQuarterlyReport(report);

            List<PrivilegedSurveillance> privilegedSurvForReport = quarterlySurvMapDao.getByReport(report.getId());
            //inject privileged surv data into report
            for (PrivilegedSurveillance privSurv : privilegedSurvForReport) {
                if (relevantListing.getId().equals(privSurv.getCertifiedProductId())) {
                    relevantListing.getSurveillances().add(privSurv);
                }
            }
        }

        return relevantListing;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "#report)")
    public List<RelevantListing> getRelevantListings(QuarterlyReport report) {
        List<RelevantListing> relevantListings = quarterlyDao.getListingsWithSurveillanceDuring(report.getAcb().getId(),
                report.getStartDay(), report.getEndDay());
        List<PrivilegedSurveillance> privilegedSurvForReport = quarterlySurvMapDao.getByReport(report.getId());

        //inject privileged surv data into report
        for (RelevantListing relevantListing : relevantListings) {
            for (PrivilegedSurveillance privSurv : privilegedSurvForReport) {
                if (relevantListing.getId().equals(privSurv.getCertifiedProductId())) {
                    relevantListing.getSurveillances().add(privSurv);
                }
            }
        }
        return relevantListings;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "#report)")
    public List<RelevantListing> getRelevantListings(List<QuarterlyReport> quarterlyReports) {
        Long acbId = quarterlyReports.get(0).getAcb().getId();
        //find the date range encompassing all the reports
        LocalDate startDay = quarterlyReports.get(0).getStartDay();
        LocalDate endDay = quarterlyReports.get(0).getEndDay();
        for (QuarterlyReport report : quarterlyReports) {
            if (report.getStartDay().isBefore(startDay)) {
                startDay = report.getStartDay();
            }
            if (report.getEndDay().isAfter(endDay)) {
                endDay = report.getEndDay();
            }
        }

        List<RelevantListing> relevantListings = quarterlyDao.getListingsWithSurveillanceDuring(acbId, startDay, endDay);
        List<PrivilegedSurveillance> privilegedSurvForReport = quarterlySurvMapDao
                .getByReports(quarterlyReports.stream().map(report -> report.getId()).collect(Collectors.toList()));

        //inject privileged surv data into report
        for (RelevantListing relevantListing : relevantListings) {
            for (PrivilegedSurveillance privSurv : privilegedSurvForReport) {
                if (relevantListing.getId().equals(privSurv.getCertifiedProductId())) {
                    relevantListing.getSurveillances().add(privSurv);
                }
            }
        }
        return relevantListings;
    }

    @Transactional
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "returnObject)")
    public QuarterlyReport getQuarterlyReport(Long id) throws EntityRetrievalException {
        QuarterlyReport report = quarterlyDao.getById(id);
        return report;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).EXPORT_QUARTERLY, "
            + "#quarterlyReportId)")
    public ChplOneTimeTrigger exportQuarterlyReportAsBackgroundJob(Long quarterlyReportId)
            throws ValidationException, SchedulerException, UserRetrievalException {
        UserDTO jobUser = null;
        try {
            jobUser = userManager.getById(AuthUtil.getCurrentUser().getId());
        } catch (UserRetrievalException ex) {
            LOGGER.error("Could not find user to execute job.");
        }

        ChplOneTimeTrigger exportQuarterlyReportTrigger = new ChplOneTimeTrigger();
        ChplJob expoertQuarterlyReportJob = new ChplJob();
        expoertQuarterlyReportJob.setName(QuarterlyReportGenerationJob.JOB_NAME);
        expoertQuarterlyReportJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(QuarterlyReportGenerationJob.QUARTERLY_REPORT_ID_KEY, quarterlyReportId);
        jobDataMap.put(QuarterlyReportGenerationJob.USER_KEY, jobUser);
        expoertQuarterlyReportJob.setJobDataMap(jobDataMap);
        exportQuarterlyReportTrigger.setJob(expoertQuarterlyReportJob);
        exportQuarterlyReportTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.FIVE_SECONDS_IN_MILLIS);
        exportQuarterlyReportTrigger = schedulerManager.createBackgroundJobTrigger(exportQuarterlyReportTrigger);

        return exportQuarterlyReportTrigger;
    }

    private void reviewQuarterlyReportForDeprecatedFields(QuarterlyReport toCreate) throws ValidationException {
        if (toCreate.isAcknowledgeWarnings()) {
            return;
        }
        //if fields are used that are deprecated create a Set of warnings
        //and throw a ValidationException with those warnings from here
    }

    private void reviewQuarterlyReportForDeprecatedFields(QuarterlyReport toUpdate, QuarterlyReport existing) throws ValidationException {
        if (toUpdate.isAcknowledgeWarnings()) {
            return;
        }
        //if fields that are deprecated have updated data, create a Set of warnings
        //using "deprecated.field.update" and throw a ValidationException with those warnings from here
    }

    private void reviewQuarterlyReportToCreate(QuarterlyReport toCreate)
            throws EntityCreationException, InvalidArgumentsException, JsonProcessingException, EntityRetrievalException {
        //Quarterly report has to have an ACB, year, and quarter
        if (toCreate.getYear() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.quarterlySurveillance.missingYear"));
        }
        if (toCreate.getAcb() == null || toCreate.getAcb().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.quarterlySurveillance.missingAcb"));
        }

        Quarter quarter = null;
        if(StringUtils.isEmpty(toCreate.getQuarter())) {
            throw new InvalidArgumentsException("report.quarterlySurveillance.missingQuarter");
        } else {
            quarter = quarterDao.getByName(toCreate.getQuarter());
            if (quarter == null
                    || (quarter.getId() == null && StringUtils.isEmpty(quarter.getName()))) {
                throw new InvalidArgumentsException(
                        msgUtil.getMessage("report.quarterlySurveillance.badQuarter", toCreate.getQuarter()));
            }
        }

        //make sure there's not already a quarterly report for this acb and year and quarter
        QuarterlyReport existingQuarterlyReport =
                quarterlyDao.getByQuarterAndAcbAndYear(quarter.getId(),
                        toCreate.getAcb().getId(),
                        toCreate.getYear());
        if (existingQuarterlyReport != null) {
            throw new EntityCreationException(msgUtil.getMessage("report.quarterlySurveillance.exists"));
        }
    }

    private void copyPreviousReportDataIntoNextReport(QuarterlyReport nextReport) {
        QuarterlyReport prevReport = getPreviousReport(nextReport);
        if (prevReport != null) {
            //copy the four data fields
            nextReport.setSurveillanceActivitiesAndOutcomes(prevReport.getSurveillanceActivitiesAndOutcomes());
            nextReport.setPrioritizedElementSummary(prevReport.getPrioritizedElementSummary());
            nextReport.setReactiveSurveillanceSummary(prevReport.getReactiveSurveillanceSummary());
            nextReport.setDisclosureRequirementsSummary(prevReport.getDisclosureRequirementsSummary());
            try {
                quarterlyDao.update(nextReport);
            } catch (Exception ex) {
                LOGGER.error("Could not copy basic report data from "
                + prevReport.getQuarter() + " into "
                + nextReport.getQuarter());
            }

            //copy privileged surveillance data
            List<PrivilegedSurveillance> prevReportSurveillanceWithPrivilegedData =
                    quarterlySurvMapDao.getByReport(prevReport.getId());
            List<RelevantListing> nextReportListingsWithSurveillance =
                    quarterlyDao.getListingsWithSurveillanceDuring(nextReport.getAcb().getId(), nextReport.getStartDay(), nextReport.getEndDay());
            for (RelevantListing nextReportListingWithSurv : nextReportListingsWithSurveillance) {
                for (PrivilegedSurveillance nextReportSurv : nextReportListingWithSurv.getSurveillances()) {
                    //for each surveillance relevant to the new report, see if it has
                    //and privileged data entered in the previous report
                    for (PrivilegedSurveillance prevReportSurv : prevReportSurveillanceWithPrivilegedData) {
                        if (nextReportSurv.getId().equals(prevReportSurv.getId())) {
                            nextReportSurv.setQuarterlyReport(nextReport);
                            nextReportSurv.setK1Reviewed(prevReportSurv.getK1Reviewed());
                            nextReportSurv.setGroundsForInitiating(prevReportSurv.getGroundsForInitiating());
                            nextReportSurv.setNonconformityCauses(prevReportSurv.getNonconformityCauses());
                            nextReportSurv.setNonconformityNature(prevReportSurv.getNonconformityNature());
                            nextReportSurv.setStepsToSurveil(prevReportSurv.getStepsToSurveil());
                            nextReportSurv.setStepsToEngage(prevReportSurv.getStepsToEngage());
                            nextReportSurv.setAdditionalCostsEvaluation(prevReportSurv.getAdditionalCostsEvaluation());
                            nextReportSurv.setLimitationsEvaluation(prevReportSurv.getLimitationsEvaluation());
                            nextReportSurv.setNondisclosureEvaluation(prevReportSurv.getNondisclosureEvaluation());
                            nextReportSurv.setDirectionDeveloperResolution(
                                    prevReportSurv.getDirectionDeveloperResolution());
                            nextReportSurv.setCompletedCapVerification(prevReportSurv.getCompletedCapVerification());
                            nextReportSurv.setSurveillanceOutcome(prevReportSurv.getSurveillanceOutcome());
                            nextReportSurv.setSurveillanceOutcomeOther(prevReportSurv.getSurveillanceOutcomeOther());
                            nextReportSurv.setSurveillanceProcessType(prevReportSurv.getSurveillanceProcessType());
                            nextReportSurv.setSurveillanceProcessTypeOther(
                                    prevReportSurv.getSurveillanceProcessTypeOther());
                            try {
                                quarterlySurvMapDao.create(nextReport.getId(), nextReportSurv);
                            } catch (Exception ex) {
                                LOGGER.error("Could not copy privileged surveillance for surveillance "
                                        + nextReportSurv.getId()
                                        + " from " + prevReport.getQuarter() + " into "
                                        + nextReport.getQuarter());
                            }
                        }
                    }
                }
            }

        } else {
            LOGGER.warn("Could not find a previous quarterly report to initialize data for "
                    + nextReport.getAcb().getName() + "'s "
                    + nextReport.getQuarter() + " report.");
        }
    }

    private QuarterlyReport getPreviousReport(QuarterlyReport report) {
        LocalDate previousReportDate = report.getStartDay().minusDays(2);
        //now the calendar points to the last day of the previous quarter
        //so just find the report that contains that date
        QuarterlyReport prevReport = null;
        List<QuarterlyReport> allReports = quarterlyDao.getAll();
        for (QuarterlyReport currReport : allReports) {
            if (currReport.getAcb().getId().equals(report.getAcb().getId())
                    && currReport.getStartDay().isBefore(previousReportDate)
                    && currReport.getEndDay().isAfter(previousReportDate)) {
                prevReport = currReport;
            }
        }
        return prevReport;
    }
}
