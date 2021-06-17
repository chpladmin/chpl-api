package gov.healthit.chpl.manager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.ComplaintDAO;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.complaint.ComplainantType;
import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.manager.rules.complaints.ComplaintValidationContext;
import gov.healthit.chpl.manager.rules.complaints.ComplaintValidationFactory;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class ComplaintManager extends SecuredManager {

    private ComplaintDAO complaintDAO;
    private ComplaintValidationFactory complaintValidationFactory;
    private CertifiedProductDAO certifiedProductDAO;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ActivityManager activityManager;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public ComplaintManager(ComplaintDAO complaintDAO,
            ComplaintValidationFactory complaintValidationFactory, CertifiedProductDAO certifiedProductDAO,
            ChplProductNumberUtil chplProductNumberUtil, ErrorMessageUtil errorMessageUtil,
            ActivityManager activityManager) {
        this.complaintDAO = complaintDAO;
        this.complaintValidationFactory = complaintValidationFactory;
        this.certifiedProductDAO = certifiedProductDAO;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.errorMessageUtil = errorMessageUtil;
        this.activityManager = activityManager;
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
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).GET_ALL, filterObject)")
    public List<Complaint> getAllComplaints() {
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
    public void delete(Long complaintId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        Complaint complaint = complaintDAO.getComplaint(complaintId);
        if (complaint != null) {
            complaintDAO.delete(complaint);

            activityManager.addActivity(ActivityConcept.COMPLAINT, complaint.getId(), "Complaint has been deleted", complaint,
                    null);
        }
    }

    private List<String> runUpdateValidations(Complaint complaint) {
        List<ValidationRule<ComplaintValidationContext>> rules = new ArrayList<ValidationRule<ComplaintValidationContext>>();
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.ACB_CHANGE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.COMPLAINT_TYPE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.RECEIVED_DATE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.ACB_COMPLAINT_ID));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.SUMMARY));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.LISTINGS));
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
