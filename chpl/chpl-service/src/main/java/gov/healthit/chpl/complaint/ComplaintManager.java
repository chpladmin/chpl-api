package gov.healthit.chpl.complaint;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.complaint.domain.ComplainantType;
import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.complaint.rules.ComplaintValidationContext;
import gov.healthit.chpl.complaint.rules.ComplaintValidationFactory;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.scheduler.job.complaints.ComplaintsReportJob;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ComplaintManager extends SecuredManager {

    private ComplaintDAO complaintDAO;
    private ComplaintValidationFactory complaintValidationFactory;
    private CertifiedProductDAO certifiedProductDAO;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ActivityManager activityManager;
    private SchedulerManager schedulerManager;
    private UserManager userManager;
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public ComplaintManager(ComplaintDAO complaintDAO,
            ComplaintValidationFactory complaintValidationFactory, CertifiedProductDAO certifiedProductDAO,
            ChplProductNumberUtil chplProductNumberUtil, ErrorMessageUtil errorMessageUtil,
            ActivityManager activityManager, SchedulerManager schedulerManager,
            UserManager userManager, ResourcePermissions resourcePermissions) {
        this.complaintDAO = complaintDAO;
        this.complaintValidationFactory = complaintValidationFactory;
        this.certifiedProductDAO = certifiedProductDAO;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.errorMessageUtil = errorMessageUtil;
        this.activityManager = activityManager;
        this.schedulerManager = schedulerManager;
        this.userManager = userManager;
        this.resourcePermissions = resourcePermissions;
    }

    @Transactional
    public Set<KeyValueModel> getComplainantTypes() {
        List<ComplainantType> complaintTypes = complaintDAO.getComplainantTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();
        for (ComplainantType complaintType : complaintTypes) {
            results.add(new KeyValueModel(complaintType.getId(), complaintType.getName()));
        }
        return results;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).GET_ALL)")
    public List<Complaint> getAllComplaints() {
        if (resourcePermissions.isUserRoleAcbAdmin()) {
            return complaintDAO.getAllComplaintsForAcbs(resourcePermissions.getAllAcbsForCurrentUser().stream()
                    .map(acb -> acb.getId())
                    .toList());
        }
        return complaintDAO.getAllComplaints();
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).GET_ALL)")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).GET_ALL, filterObject)")
    public List<Complaint> getAllComplaintsBetweenDates(CertificationBodyDTO acb, LocalDate startDate,
            LocalDate endDate) {
        return complaintDAO.getAllComplaintsBetweenDates(acb.getId(), startDate, endDate);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).CREATE, #complaint)")
    @CacheEvict(value = { CacheNames.COMPLAINTS }, allEntries = true)
    public Complaint create(Complaint complaint)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException {
        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(runCreateValidations(complaint));
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        Complaint newComplaint = complaintDAO.create(complaint);

        activityManager.addActivity(ActivityConcept.COMPLAINT, newComplaint.getId(), "Complaint has been created", null,
                newComplaint);
        return newComplaint;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).UPDATE, #complaint)")
    @CacheEvict(value = { CacheNames.COMPLAINTS }, allEntries = true)
    public Complaint update(Complaint complaint)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException {
        Complaint originalFromDB = complaintDAO.getComplaint(complaint.getId());
        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(runUpdateValidations(complaint));
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        Complaint updatedComplaint = complaintDAO.update(complaint);
        activityManager.addActivity(ActivityConcept.COMPLAINT, updatedComplaint.getId(), "Complaint has been updated",
                originalFromDB, updatedComplaint);

        return updatedComplaint;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).DELETE, #complaintId)")
    @CacheEvict(value = { CacheNames.COMPLAINTS }, allEntries = true)
    public void delete(Long complaintId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        Complaint complaint = complaintDAO.getComplaint(complaintId);
        if (complaint != null) {
            complaintDAO.delete(complaint);

            activityManager.addActivity(ActivityConcept.COMPLAINT, complaint.getId(), "Complaint has been deleted", complaint,
                    null);
        }
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).DOWNLOAD_ALL)")
    public ChplOneTimeTrigger triggerComplaintsReport() throws SchedulerException, ValidationException {
        UserDTO jobUser = null;
        try {
            jobUser = userManager.getById(AuthUtil.getCurrentUser().getId());
        } catch (UserRetrievalException ex) {
            LOGGER.error("Could not find user to execute job.");
        }

        ChplOneTimeTrigger complaintsReportTrigger = new ChplOneTimeTrigger();
        ChplJob complaintsReportJob = new ChplJob();
        complaintsReportJob.setName(ComplaintsReportJob.JOB_NAME);
        complaintsReportJob.setGroup(SchedulerManager.CHPL_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(ComplaintsReportJob.EMAIL_KEY, jobUser.getEmail());
        complaintsReportJob.setJobDataMap(jobDataMap);
        complaintsReportTrigger.setJob(complaintsReportJob);
        complaintsReportTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.DELAY_BEFORE_BACKGROUND_JOB_START);
        complaintsReportTrigger = schedulerManager.createBackgroundJobTrigger(complaintsReportTrigger);
        return complaintsReportTrigger;

    }

    private List<String> runUpdateValidations(Complaint complaint) {
        List<ValidationRule<ComplaintValidationContext>> rules = new ArrayList<ValidationRule<ComplaintValidationContext>>();
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.ACB_CHANGE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.COMPLAINT_TYPE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.RECEIVED_DATE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.ACB_COMPLAINT_ID));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.SUMMARY));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.LISTINGS));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.SURVEILLANCE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.CLOSED_DATE));
        return runValidations(rules, complaint);
    }

    private List<String> runCreateValidations(Complaint complaint) {
        List<ValidationRule<ComplaintValidationContext>> rules = new ArrayList<ValidationRule<ComplaintValidationContext>>();
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.OPEN_STATUS));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.COMPLAINT_TYPE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.RECEIVED_DATE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.ACB_COMPLAINT_ID));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.SUMMARY));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.LISTINGS));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.SURVEILLANCE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.CLOSED_DATE));

        return runValidations(rules, complaint);
    }

    private List<String> runValidations(List<ValidationRule<ComplaintValidationContext>> rules, Complaint complaint) {
        List<String> errorMessages = new ArrayList<String>();
        ComplaintValidationContext context = new ComplaintValidationContext(complaint, complaintDAO, certifiedProductDAO,
                errorMessageUtil, chplProductNumberUtil);

        for (ValidationRule<ComplaintValidationContext> rule : rules) {
            if (!rule.isValid(context)) {
                errorMessages.addAll(rule.getMessages());
            }
        }
        return errorMessages;
    }
}
