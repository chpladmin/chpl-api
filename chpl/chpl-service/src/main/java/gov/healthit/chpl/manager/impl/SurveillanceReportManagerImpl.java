package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.surveillance.report.AnnualReportDAO;
import gov.healthit.chpl.dao.surveillance.report.QuarterDAO;
import gov.healthit.chpl.dao.surveillance.report.QuarterlyReportDAO;
import gov.healthit.chpl.dao.surveillance.report.PrivilegedSurveillanceDAO;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.concept.JobTypeConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportExclusionDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.dto.surveillance.report.PrivilegedSurveillanceDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceOutcomeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceProcessTypeDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.JobManager;
import gov.healthit.chpl.manager.SurveillanceReportManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Service
public class SurveillanceReportManagerImpl extends SecuredManager implements SurveillanceReportManager {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceReportManagerImpl.class);

    private ActivityManager activityManager;
    private UserManager userManager;
    private JobManager jobManager;
    private QuarterlyReportDAO quarterlyDao;
    private PrivilegedSurveillanceDAO quarterlySurvMapDao;
    private AnnualReportDAO annualDao;
    private QuarterDAO quarterDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public SurveillanceReportManagerImpl(final ActivityManager activityManager,
            final UserManager userManager,
            final JobManager jobManager, final QuarterlyReportDAO quarterlyDao,
            final PrivilegedSurveillanceDAO quarterlySurvMapDao,
            final AnnualReportDAO annualDao, final QuarterDAO quarterDao,
            final ErrorMessageUtil msgUtil) {
        this.activityManager = activityManager;
        this.userManager = userManager;
        this.jobManager = jobManager;
        this.quarterlyDao = quarterlyDao;
        this.quarterlySurvMapDao = quarterlySurvMapDao;
        this.annualDao = annualDao;
        this.quarterDao = quarterDao;
        this.msgUtil = msgUtil;
    }

    @Override
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

    @Override
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

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).CREATE_ANNUAL, #toCreate)")
    public AnnualReportDTO createAnnualReport(final AnnualReportDTO toCreate)
    throws EntityCreationException, InvalidArgumentsException, JsonProcessingException, EntityRetrievalException {
        //Annual report has to be associated with a year and an ACB

        if (toCreate == null || toCreate.getYear() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingYear"));
        } else if (toCreate.getAcb() == null || toCreate.getAcb().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingAcb"));
        }

        //make sure there's not already an annual report for this acb and year
        AnnualReportDTO existingAnnualReport =
                annualDao.getByAcbAndYear(toCreate.getAcb().getId(), toCreate.getYear());
        if (existingAnnualReport != null) {
            throw new EntityCreationException(msgUtil.getMessage("report.annualSurveillance.exists"));
        }

        AnnualReportDTO created = annualDao.create(toCreate);
        AnnualReportDTO afterAnnualReport = annualDao.getById(created.getId());
        activityManager.addActivity(ActivityConcept.ANNUAL_REPORT, created.getId(),
                "Created annual report.", null, afterAnnualReport);
        return created;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).UPDATE_ANNUAL, #toUpdate)")
    public AnnualReportDTO updateAnnualReport(final AnnualReportDTO toUpdate)
    throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        AnnualReportDTO before = annualDao.getById(toUpdate.getId());
        AnnualReportDTO updated = annualDao.update(toUpdate);
        activityManager.addActivity(ActivityConcept.ANNUAL_REPORT, updated.getId(),
                "Updated annual report.", before, updated);
        return updated;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).DELETE_ANNUAL, #id)")
    public void deleteAnnualReport(final Long id)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        AnnualReportDTO before = annualDao.getById(id);
        annualDao.delete(id);
        activityManager.addActivity(ActivityConcept.ANNUAL_REPORT, id,
                "Deleted annual report.", before, null);
    }

    /**
     * Returns all the annual reports the current user has access to.
     */
    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_ANNUAL)")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_ANNUAL, filterObject)")
    public List<AnnualReportDTO> getAnnualReports() {
        return annualDao.getAll();
    }

    /**
     * Gets the quarterly report by ID if the user has access.
     */
    @Override
    @Transactional
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_ANNUAL,"
            + "returnObject)")
    public AnnualReportDTO getAnnualReport(final Long id) throws EntityRetrievalException {
        return annualDao.getById(id);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).EXPORT_ANNUAL, "
            + "#id)")
    public JobDTO exportAnnualReportAsBackgroundJob(final Long id)
            throws EntityRetrievalException, EntityCreationException, UserRetrievalException, IOException {
        // figure out the user
        UserDTO currentUser = userManager.getById(AuthUtil.getCurrentUser().getId());

        JobTypeDTO jobType = null;
        List<JobTypeDTO> jobTypes = jobManager.getAllJobTypes();
        for (JobTypeDTO jt : jobTypes) {
            if (jt.getName().equalsIgnoreCase(JobTypeConcept.EXPORT_ANNUAL.getName())) {
                jobType = jt;
            }
        }

        JobDTO toCreate = new JobDTO();
        //job data is the quarterly report id
        toCreate.setData(id.toString());
        toCreate.setUser(currentUser);
        toCreate.setJobType(jobType);
        JobDTO insertedJob = jobManager.createJob(toCreate);
        JobDTO createdJob = jobManager.getJobById(insertedJob.getId());
        jobManager.start(createdJob);
        JobDTO startedJob = jobManager.getJobById(insertedJob.getId());

        AnnualReportDTO report = annualDao.getById(id);
        activityManager.addActivity(ActivityConcept.ANNUAL_REPORT, id,
                "Exported annual report.", null, report);
        return startedJob;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).CREATE_QUARTERLY, #toCreate)")
    public QuarterlyReportDTO createQuarterlyReport(final QuarterlyReportDTO toCreate)
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
            QuarterDTO quarter = quarterDao.getByName(toCreate.getQuarter().getName());
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
        QuarterlyReportDTO created = quarterlyDao.create(toCreate);
        copyPreviousReportDataIntoNextReport(created);
        QuarterlyReportDTO afterQuarterlyReport = quarterlyDao.getById(created.getId());
        activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT, created.getId(),
                "Created quarterly report.", null, afterQuarterlyReport);
        return created;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).CREATE_QUARTERLY, #report)")
    public QuarterlyReportExclusionDTO createQuarterlyReportExclusion(final QuarterlyReportDTO report,
            final Long listingId, final String reason)
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

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).UPDATE_QUARTERLY, #toUpdate)")
    public QuarterlyReportDTO updateQuarterlyReport(final QuarterlyReportDTO toUpdate)
    throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        QuarterlyReportDTO before = quarterlyDao.getById(toUpdate.getId());
        QuarterlyReportDTO updated = quarterlyDao.update(toUpdate);
        activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT, updated.getId(),
                "Updated quarterly report.", before, updated);
        return updated;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).UPDATE_QUARTERLY, #report)")
    public QuarterlyReportExclusionDTO updateQuarterlyReportExclusion(final QuarterlyReportDTO report,
            final Long listingId, final String reason)
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

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).UPDATE_QUARTERLY, #toUpdate.quarterlyReport)")
    public PrivilegedSurveillanceDTO createOrUpdateQuarterlyReportSurveillanceMap(
            final PrivilegedSurveillanceDTO toUpdate)
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

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).DELETE_QUARTERLY, #id)")
    public void deleteQuarterlyReport(final Long id)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        QuarterlyReportDTO report = quarterlyDao.getById(id);
        quarterlyDao.delete(id);
        activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT, id,
                "Deleted quarterly report.", report, null);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).DELETE_QUARTERLY, #reportId)")
    public void deleteQuarterlyReportExclusion(final Long reportId, final Long listingId) 
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

    /**
     * Returns all the quarterly reports the current user has access to.
     */
    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY)")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY, filterObject)")
    public List<QuarterlyReportDTO> getQuarterlyReports() {
        List<QuarterlyReportDTO> reports = quarterlyDao.getAll();
        return reports;
    }

    /**
     * Gets the quarterly reports for a specific ACB and year.
     */
    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY)")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY, filterObject)")
    public List<QuarterlyReportDTO> getQuarterlyReports(final Long acbId, final Integer year) {
        List<QuarterlyReportDTO> reports = quarterlyDao.getByAcbAndYear(acbId, year);
        return reports;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "#report)")
    public QuarterlyReportExclusionDTO getExclusion(final QuarterlyReportDTO report, final Long listingId) {
        QuarterlyReportExclusionDTO existingExclusion =
                quarterlyDao.getExclusion(report.getId(), listingId);
        return existingExclusion;
    }

    /**
     * Get the relevant listing object (including whether it is excluded and the reason)
     * for a specific listing and dates.
     * Returns null if the listing is not relevant during the dates.
     * @param report
     * @param listingId
     * @return
     */
    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "#report)")
    public QuarterlyReportRelevantListingDTO getRelevantListing(final QuarterlyReportDTO report, final Long listingId) {
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

    /**
     * Returns the listings owned by the ACB associated with the report
     * included boolean fields about whether they are marked as excluded.
     */
    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "#report)")
    public List<QuarterlyReportRelevantListingDTO> getRelevantListings(final QuarterlyReportDTO report) {
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

    /**
     * Returns the listings owned by the ACB of the quarterly report
     * that had open surveillance during the quarter
     * included boolean fields about whether they are marked as excluded.
     */
    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "#report)")
    public List<QuarterlyReportRelevantListingDTO> getListingsWithRelevantSurveillance(final QuarterlyReportDTO report) {
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

    /**
     * Gets the quarterly report by ID if the user has access.
     */
    @Override
    @Transactional
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "returnObject)")
    public QuarterlyReportDTO getQuarterlyReport(final Long id) throws EntityRetrievalException {
        QuarterlyReportDTO report = quarterlyDao.getById(id);
        return report;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).EXPORT_QUARTERLY, "
            + "#id)")
    public JobDTO exportQuarterlyReportAsBackgroundJob(final Long id)
            throws EntityRetrievalException, EntityCreationException, UserRetrievalException, IOException {
        // figure out the user
        UserDTO currentUser = userManager.getById(AuthUtil.getCurrentUser().getId());

        JobTypeDTO jobType = null;
        List<JobTypeDTO> jobTypes = jobManager.getAllJobTypes();
        for (JobTypeDTO jt : jobTypes) {
            if (jt.getName().equalsIgnoreCase(JobTypeConcept.EXPORT_QUARTERLY.getName())) {
                jobType = jt;
            }
        }

        JobDTO toCreate = new JobDTO();
        //job data is the quarterly report id
        toCreate.setData(id.toString());
        toCreate.setUser(currentUser);
        toCreate.setJobType(jobType);
        JobDTO insertedJob = jobManager.createJob(toCreate);
        JobDTO createdJob = jobManager.getJobById(insertedJob.getId());
        jobManager.start(createdJob);
        JobDTO startedJob = jobManager.getJobById(insertedJob.getId());
        QuarterlyReportDTO report = quarterlyDao.getById(id);
        activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT, id,
                "Exported quarterly report.", null, report);
        return startedJob;
    }

    private void copyPreviousReportDataIntoNextReport(final QuarterlyReportDTO nextReport) {
        QuarterlyReportDTO prevReport = getPreviousReport(nextReport);
        if (prevReport != null) {
            //copy the four data fields
            nextReport.setActivitiesOutcomesSummary(prevReport.getActivitiesOutcomesSummary());
            nextReport.setPrioritizedElementSummary(prevReport.getPrioritizedElementSummary());
            nextReport.setReactiveSummary(prevReport.getReactiveSummary());
            nextReport.setTransparencyDisclosureSummary(prevReport.getTransparencyDisclosureSummary());
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

    private QuarterlyReportDTO getPreviousReport(final QuarterlyReportDTO report) {
        Calendar createdReportStartCal = Calendar.getInstance();
        createdReportStartCal.setTime(report.getStartDate());
        createdReportStartCal.add(Calendar.DATE, -1);
        //now the calendar points to the last day of the previous quarter
        //so just find the report that contains that date
        QuarterlyReportDTO prevReport = null;
        List<QuarterlyReportDTO> allReports = quarterlyDao.getAll();
        for (QuarterlyReportDTO currReport : allReports) {
            if (currReport.getAcb().getId().equals(report.getAcb().getId())
                    && currReport.getStartDate().getTime() <= createdReportStartCal.getTimeInMillis()
                    && currReport.getEndDate().getTime() >= createdReportStartCal.getTimeInMillis()) {
                prevReport = currReport;
            }
        }
        return prevReport;
    }
}
