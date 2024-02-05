package gov.healthit.chpl.surveillance.report;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
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
import gov.healthit.chpl.surveillance.report.domain.Quarter;
import gov.healthit.chpl.surveillance.report.domain.QuarterlyReport;
import gov.healthit.chpl.surveillance.report.dto.PrivilegedSurveillanceDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportExclusionDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.surveillance.report.dto.SurveillanceOutcomeDTO;
import gov.healthit.chpl.surveillance.report.dto.SurveillanceProcessTypeDTO;
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
        List<SurveillanceOutcomeDTO> outcomes = quarterlySurvMapDao.getSurveillanceOutcomes();
        Set<KeyValueModel> result = new HashSet<KeyValueModel>();
        for (SurveillanceOutcomeDTO outcome : outcomes) {
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
        List<SurveillanceProcessTypeDTO> pts = quarterlySurvMapDao.getSurveillanceProcessTypes();
        Set<KeyValueModel> result = new HashSet<KeyValueModel>();
        for (SurveillanceProcessTypeDTO pt : pts) {
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
    public QuarterlyReportDTO createQuarterlyReport(QuarterlyReport createRequest)
    throws EntityCreationException, InvalidArgumentsException, JsonProcessingException, EntityRetrievalException, ValidationException {

        reviewQuarterlyReportForDeprecatedFields(createRequest);
        //create the report
        QuarterlyReportDTO toCreate = QuarterlyReportDTO.builder()
                .year(createRequest.getYear())
                .acb(CertificationBody.builder()
                        .id(createRequest.getAcb().getId())
                        .build())
                .quarter(Quarter.builder()
                        .name(createRequest.getQuarter())
                        .build())
                .activitiesOutcomesSummary(createRequest.getSurveillanceActivitiesAndOutcomes())
                .prioritizedElementSummary(createRequest.getPrioritizedElementSummary())
                .reactiveSurveillanceSummary(createRequest.getReactiveSurveillanceSummary())
                .disclosureRequirementsSummary(createRequest.getDisclosureRequirementsSummary())
                .build();
        reviewQuarterlyReportToCreate(toCreate);
        QuarterlyReportDTO created = quarterlyDao.create(toCreate);
        copyPreviousReportDataIntoNextReport(created);
        QuarterlyReportDTO afterQuarterlyReport = quarterlyDao.getById(created.getId());
        activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT, created.getId(),
                "Created quarterly report.", null, afterQuarterlyReport);
        return created;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).CREATE_QUARTERLY, #report)")
    public QuarterlyReportExclusionDTO createQuarterlyReportExclusion(QuarterlyReportDTO report,
            Long listingId, String reason)
            throws EntityCreationException, InvalidArgumentsException, JsonProcessingException, EntityRetrievalException {
        //make sure there's not already an exclusion for this report and listing
        QuarterlyReportExclusionDTO existingExclusion =
                quarterlyDao.getExclusion(report.getId(), listingId);
        if (existingExclusion != null) {
            throw new EntityCreationException(
                    msgUtil.getMessage("report.quarterlySurveillance.exclusion.exists", report.getQuarter().getName(), listingId));
        }

        //confirm that the specified listing is relevant to the report
        boolean isRelevant = quarterlyDao.isListingRelevant(report.getAcb().getId(), listingId);
        if (!isRelevant) {
            throw new EntityCreationException(
                    msgUtil.getMessage("report.quarterlySurveillance.exclusion.notRelevant", listingId, report.getQuarter().getName()));
        }

        QuarterlyReportRelevantListingDTO beforeRelevantListing = getRelevantListing(report, listingId);
        QuarterlyReportExclusionDTO toCreate = new QuarterlyReportExclusionDTO();
        toCreate.setQuarterlyReportId(report.getId());
        toCreate.setListingId(listingId);
        toCreate.setReason(reason);
        QuarterlyReportExclusionDTO created = quarterlyDao.createExclusion(toCreate);
        QuarterlyReportRelevantListingDTO afterRelevantListing = getRelevantListing(report, listingId);
        activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT_LISTING, report.getId(),
                "Updated relevant listing for quarterly report.", beforeRelevantListing, afterRelevantListing);
        return created;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).UPDATE_QUARTERLY, #updateRequest)")
    public QuarterlyReport updateQuarterlyReport(QuarterlyReport updateRequest)
    throws EntityRetrievalException, JsonProcessingException, EntityCreationException, ValidationException {
        QuarterlyReportDTO reportToUpdate = getQuarterlyReport(updateRequest.getId());
        reviewQuarterlyReportForDeprecatedFields(updateRequest, reportToUpdate);
        //above line throws entity retrieval exception if bad id
        reportToUpdate.setActivitiesOutcomesSummary(updateRequest.getSurveillanceActivitiesAndOutcomes());
        reportToUpdate.setPrioritizedElementSummary(updateRequest.getPrioritizedElementSummary());
        reportToUpdate.setReactiveSurveillanceSummary(updateRequest.getReactiveSurveillanceSummary());
        reportToUpdate.setDisclosureRequirementsSummary(updateRequest.getDisclosureRequirementsSummary());
        QuarterlyReportDTO before = quarterlyDao.getById(reportToUpdate.getId());
        QuarterlyReportDTO updated = quarterlyDao.update(reportToUpdate);
        activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT, updated.getId(),
                "Updated quarterly report.", before, updated);
        return new QuarterlyReport(updated);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).UPDATE_QUARTERLY, #report)")
    public QuarterlyReportExclusionDTO updateQuarterlyReportExclusion(QuarterlyReportDTO report,
            Long listingId, String reason)
                    throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        //make sure there is already an exclusion for this report and listing
        QuarterlyReportExclusionDTO existingExclusion =
                quarterlyDao.getExclusion(report.getId(), listingId);
        if (existingExclusion == null) {
            throw new EntityRetrievalException(
                    msgUtil.getMessage("report.quarterlySurveillance.exclusion.doesNotExist", report.getQuarter().getName(), listingId));
        }

        QuarterlyReportRelevantListingDTO beforeRelevantListing = getRelevantListing(report, listingId);
        existingExclusion.setReason(reason);
        QuarterlyReportExclusionDTO updated = quarterlyDao.updateExclusion(existingExclusion);
        QuarterlyReportRelevantListingDTO afterRelevantListing = getRelevantListing(report, listingId);
        activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT_LISTING, report.getId(),
                "Updated relevant listing for quarterly report.", beforeRelevantListing, afterRelevantListing);
        return updated;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).UPDATE_QUARTERLY, #toUpdate.quarterlyReport)")
    public PrivilegedSurveillanceDTO createOrUpdateQuarterlyReportSurveillanceMap(
            PrivilegedSurveillanceDTO toUpdate)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        // make sure passed-in surveillance is relevant to the report i.e. that it was open at some point
        //during the reporting period
        if (!quarterlyDao.isSurveillanceRelevant(toUpdate.getQuarterlyReport(), toUpdate.getId())) {
            throw new EntityCreationException(
                    msgUtil.getMessage("report.quarterlySurveillance.surveillance.notRelevant",
                            toUpdate.getFriendlyId(), toUpdate.getQuarterlyReport().getQuarter().getName()));
        }
        PrivilegedSurveillanceDTO existing = quarterlySurvMapDao.getByReportAndSurveillance(
                toUpdate.getQuarterlyReport().getId(),
                toUpdate.getId());

        QuarterlyReportRelevantListingDTO beforeRelevantListing = getRelevantListing(
                toUpdate.getQuarterlyReport(), toUpdate.getCertifiedProductId());
        PrivilegedSurveillanceDTO result = null;
        if (existing == null) {
            result = quarterlySurvMapDao.create(toUpdate);
        } else {
            toUpdate.setMappingId(existing.getMappingId());
            result = quarterlySurvMapDao.update(toUpdate);
        }
        QuarterlyReportRelevantListingDTO afterRelevantListing = getRelevantListing(
                toUpdate.getQuarterlyReport(), toUpdate.getCertifiedProductId());
        activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT_LISTING, toUpdate.getQuarterlyReport().getId(),
                "Updated relevant listing for quarterly report.", beforeRelevantListing, afterRelevantListing);
        return result;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).DELETE_QUARTERLY, #id)")
    public void deleteQuarterlyReport(Long id)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        QuarterlyReportDTO report = quarterlyDao.getById(id);
        quarterlyDao.delete(id);
        activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT, id,
                "Deleted quarterly report.", report, null);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).DELETE_QUARTERLY, #reportId)")
    public void deleteQuarterlyReportExclusion(Long reportId, Long listingId)
        throws JsonProcessingException, EntityRetrievalException, EntityCreationException {
        QuarterlyReportDTO report = quarterlyDao.getById(reportId);
        //make sure there is already an exclusion for this report and listing
        QuarterlyReportExclusionDTO existingExclusion =
                quarterlyDao.getExclusion(reportId, listingId);
        if (existingExclusion != null) {
            try {
                QuarterlyReportRelevantListingDTO beforeRelevantListing = getRelevantListing(report, listingId);
                quarterlyDao.deleteExclusion(existingExclusion.getId());
                QuarterlyReportRelevantListingDTO afterRelevantListing = getRelevantListing(report, listingId);
                activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT_LISTING, reportId,
                        "Updated relevant listing for quarterly report.", beforeRelevantListing, afterRelevantListing);
            } catch (EntityRetrievalException ex) {
                LOGGER.error("No existing exclusion for ID " + existingExclusion.getId() + " could be deleted.");
            }
        }
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY)")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY, filterObject)")
    public List<QuarterlyReportDTO> getQuarterlyReports() {
        List<QuarterlyReportDTO> reports = quarterlyDao.getAll();
        return reports;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY)")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY, filterObject)")
    public List<QuarterlyReportDTO> getQuarterlyReports(Long acbId, Integer year) {
        List<QuarterlyReportDTO> reports = quarterlyDao.getByAcbAndYear(acbId, year);
        return reports;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "#report)")
    public QuarterlyReportExclusionDTO getExclusion(QuarterlyReportDTO report, Long listingId) {
        QuarterlyReportExclusionDTO existingExclusion =
                quarterlyDao.getExclusion(report.getId(), listingId);
        return existingExclusion;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "#report)")
    public QuarterlyReportRelevantListingDTO getRelevantListing(QuarterlyReportDTO report, Long listingId) {
        QuarterlyReportRelevantListingDTO relevantListing = quarterlyDao.getRelevantListing(listingId, report);
        if (relevantListing != null) {
            relevantListing.setQuarterlyReport(report);

            List<PrivilegedSurveillanceDTO> privilegedSurvForReport = quarterlySurvMapDao.getByReport(report.getId());
            //inject privileged surv data into relevant listing
            for (PrivilegedSurveillanceDTO relevantListingSurv : relevantListing.getSurveillances()) {
                for (PrivilegedSurveillanceDTO privSurv : privilegedSurvForReport) {
                    if (relevantListingSurv.getId().equals(privSurv.getId())) {
                        relevantListingSurv.copyPrivilegedFields(privSurv);
                    }
                }
            }

            //inject exclusion data ito relevant listing
            QuarterlyReportExclusionDTO existingExclusion =
                    quarterlyDao.getExclusion(report.getId(), relevantListing.getId());
            if (existingExclusion != null) {
                relevantListing.setExcluded(true);
                relevantListing.setExclusionReason(existingExclusion.getReason());
            }
        }

        return relevantListing;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "#report)")
    public List<QuarterlyReportRelevantListingDTO> getRelevantListings(QuarterlyReportDTO report) {
        List<QuarterlyReportRelevantListingDTO> relevantListings = quarterlyDao.getRelevantListings(report);
        List<PrivilegedSurveillanceDTO> privilegedSurvForReport = quarterlySurvMapDao.getByReport(report.getId());
        List<QuarterlyReportExclusionDTO> exclusions = quarterlyDao.getExclusions(report.getId());

        //inject privileged surv data into report
        for (QuarterlyReportRelevantListingDTO relevantListing : relevantListings) {
            for (PrivilegedSurveillanceDTO relevantListingSurv : relevantListing.getSurveillances()) {
                for (PrivilegedSurveillanceDTO privSurv : privilegedSurvForReport) {
                    if (relevantListingSurv.getId().equals(privSurv.getId())) {
                        relevantListingSurv.copyPrivilegedFields(privSurv);
                    }
                }
            }
        }

        //inject exclusion data into report
        List<QuarterlyReportRelevantListingDTO> results = new ArrayList<QuarterlyReportRelevantListingDTO>();
        for (CertifiedProductDetailsDTO relevantListing : relevantListings) {
            QuarterlyReportRelevantListingDTO qrRelevantListing = (QuarterlyReportRelevantListingDTO) relevantListing;
            for (QuarterlyReportExclusionDTO exclusion : exclusions) {
                if (exclusion.getListingId() != null && relevantListing.getId() != null
                        && exclusion.getListingId().longValue() == relevantListing.getId().longValue()) {
                    qrRelevantListing.setExcluded(true);
                    qrRelevantListing.setExclusionReason(exclusion.getReason());
                }
            }
            results.add(qrRelevantListing);
        }
        return results;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "#report)")
    public List<QuarterlyReportRelevantListingDTO> getListingsWithRelevantSurveillance(QuarterlyReportDTO report) {
        List<QuarterlyReportRelevantListingDTO> relevantListings = quarterlyDao.getListingsWithRelevantSurveillance(report);
        List<PrivilegedSurveillanceDTO> privilegedSurvForReport = quarterlySurvMapDao.getByReport(report.getId());
        List<QuarterlyReportExclusionDTO> exclusions = quarterlyDao.getExclusions(report.getId());

        //inject privileged surv data into report
        for (QuarterlyReportRelevantListingDTO relevantListing : relevantListings) {
            for (PrivilegedSurveillanceDTO relevantListingSurv : relevantListing.getSurveillances()) {
                for (PrivilegedSurveillanceDTO privSurv : privilegedSurvForReport) {
                    if (relevantListingSurv.getId().equals(privSurv.getId())) {
                        relevantListingSurv.copyPrivilegedFields(privSurv);
                    }
                }
            }
        }

        //inject exclusion data into report
        List<QuarterlyReportRelevantListingDTO> results = new ArrayList<QuarterlyReportRelevantListingDTO>();
        for (CertifiedProductDetailsDTO relevantListing : relevantListings) {
            QuarterlyReportRelevantListingDTO qrRelevantListing = (QuarterlyReportRelevantListingDTO) relevantListing;
            for (QuarterlyReportExclusionDTO exclusion : exclusions) {
                if (exclusion.getListingId() != null && relevantListing.getId() != null
                        && exclusion.getListingId().longValue() == relevantListing.getId().longValue()) {
                    qrRelevantListing.setExcluded(true);
                    qrRelevantListing.setExclusionReason(exclusion.getReason());
                }
            }
            results.add(qrRelevantListing);
        }
        return results;
    }

    @Transactional
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "returnObject)")
    public QuarterlyReportDTO getQuarterlyReport(Long id) throws EntityRetrievalException {
        QuarterlyReportDTO report = quarterlyDao.getById(id);
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

    private void reviewQuarterlyReportForDeprecatedFields(QuarterlyReport toUpdate, QuarterlyReportDTO existing) throws ValidationException {
        if (toUpdate.isAcknowledgeWarnings()) {
            return;
        }
        //if fields that are deprecated have updated data, create a Set of warnings
        //using "deprecated.field.update" and throw a ValidationException with those warnings from here
    }

    private void reviewQuarterlyReportToCreate(QuarterlyReportDTO toCreate)
            throws EntityCreationException, InvalidArgumentsException, JsonProcessingException, EntityRetrievalException {
        //Quarterly report has to have an ACB, year, and quarter
        if (toCreate.getYear() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.quarterlySurveillance.missingYear"));
        }
        if (toCreate.getAcb() == null || toCreate.getAcb().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.quarterlySurveillance.missingAcb"));
        }
        if (toCreate.getQuarter() == null
                || (toCreate.getQuarter().getId() == null && StringUtils.isEmpty(toCreate.getQuarter().getName()))) {
            throw new InvalidArgumentsException("report.quarterlySurveillance.missingQuarter");
        } else if (toCreate.getQuarter().getId() == null && toCreate.getQuarter().getName() != null) {
            Quarter quarter = quarterDao.getByName(toCreate.getQuarter().getName());
            if (quarter == null) {
                throw new InvalidArgumentsException(
                        msgUtil.getMessage("report.quarterlySurveillance.badQuarter", toCreate.getQuarter().getName()));
            }
            toCreate.setQuarter(quarter);
        }

        //make sure there's not already a quarterly report for this acb and year and quarter
        QuarterlyReportDTO existingQuarterlyReport =
                quarterlyDao.getByQuarterAndAcbAndYear(toCreate.getQuarter().getId(),
                        toCreate.getAcb().getId(),
                        toCreate.getYear());
        if (existingQuarterlyReport != null) {
            throw new EntityCreationException(msgUtil.getMessage("report.quarterlySurveillance.exists"));
        }
    }

    private void copyPreviousReportDataIntoNextReport(QuarterlyReportDTO nextReport) {
        QuarterlyReportDTO prevReport = getPreviousReport(nextReport);
        if (prevReport != null) {
            //copy the four data fields
            nextReport.setActivitiesOutcomesSummary(prevReport.getActivitiesOutcomesSummary());
            nextReport.setPrioritizedElementSummary(prevReport.getPrioritizedElementSummary());
            nextReport.setReactiveSurveillanceSummary(prevReport.getReactiveSurveillanceSummary());
            nextReport.setDisclosureRequirementsSummary(prevReport.getDisclosureRequirementsSummary());
            try {
                quarterlyDao.update(nextReport);
            } catch (Exception ex) {
                LOGGER.error("Could not copy basic report data from "
                + prevReport.getQuarter().getName() + " into "
                + nextReport.getQuarter().getName());
            }

            //copy exclusions
            List<QuarterlyReportExclusionDTO> prevReportExclusions = quarterlyDao.getExclusions(prevReport.getId());
            if (prevReportExclusions != null && prevReportExclusions.size() > 0) {
                List<QuarterlyReportRelevantListingDTO> nextReportListings = quarterlyDao.getRelevantListings(nextReport);
                for (QuarterlyReportRelevantListingDTO nextReportListing : nextReportListings) {
                    for (QuarterlyReportExclusionDTO prevReportExclusion : prevReportExclusions) {
                        if (nextReportListing.getId().equals(prevReportExclusion.getListingId())) {
                            QuarterlyReportExclusionDTO newReportExclusion = new QuarterlyReportExclusionDTO();
                            newReportExclusion.setListingId(nextReportListing.getId());
                            newReportExclusion.setQuarterlyReportId(nextReport.getId());
                            newReportExclusion.setReason(prevReportExclusion.getReason());
                            try {
                                quarterlyDao.createExclusion(newReportExclusion);
                            } catch (Exception ex) {
                                LOGGER.error("Could not copy exclusion for listing " + nextReportListing.getId()
                                        + " from " + prevReport.getQuarter().getName() + " into "
                                        + nextReport.getQuarter().getName());
                            }
                        }
                    }
                }
            }

            //copy privileged surveillance data
            List<PrivilegedSurveillanceDTO> prevReportSurveillanceWithPrivilegedData =
                    quarterlySurvMapDao.getByReport(prevReport.getId());
            List<QuarterlyReportRelevantListingDTO> nextReportListingsWithSurveillance =
                    quarterlyDao.getListingsWithRelevantSurveillance(nextReport);
            for (QuarterlyReportRelevantListingDTO nextReportListingWithSurv : nextReportListingsWithSurveillance) {
                for (PrivilegedSurveillanceDTO nextReportSurv : nextReportListingWithSurv.getSurveillances()) {
                    //for each surveillance relevant to the new report, see if it has
                    //and privileged data entered in the previous report
                    for (PrivilegedSurveillanceDTO prevReportSurv : prevReportSurveillanceWithPrivilegedData) {
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
                                quarterlySurvMapDao.create(nextReportSurv);
                            } catch (Exception ex) {
                                LOGGER.error("Could not copy privileged surveillance for surveillance "
                                        + nextReportSurv.getId()
                                        + " from " + prevReport.getQuarter().getName() + " into "
                                        + nextReport.getQuarter().getName());
                            }
                        }
                    }
                }
            }

        } else {
            LOGGER.warn("Could not find a previous quarterly report to initialize data for "
                    + nextReport.getAcb().getName() + "'s "
                    + nextReport.getQuarter().getName() + " report.");
        }
    }

    private QuarterlyReportDTO getPreviousReport(QuarterlyReportDTO report) {
        LocalDate previousReportDate = report.getStartDate().minusDays(2);
        //now the calendar points to the last day of the previous quarter
        //so just find the report that contains that date
        QuarterlyReportDTO prevReport = null;
        List<QuarterlyReportDTO> allReports = quarterlyDao.getAll();
        for (QuarterlyReportDTO currReport : allReports) {
            if (currReport.getAcb().getId().equals(report.getAcb().getId())
                    && currReport.getStartDate().isBefore(previousReportDate)
                    && currReport.getEndDate().isAfter(previousReportDate)) {
                prevReport = currReport;
            }
        }
        return prevReport;
    }
}
