package gov.healthit.chpl.manager;

import java.util.ArrayList;
import java.util.Date;
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
import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.ComplainantTypeDTO;
import gov.healthit.chpl.dto.ComplaintDTO;
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
    public ComplaintManager(final ComplaintDAO complaintDAO,
            final ComplaintValidationFactory complaintValidationFactory, final CertifiedProductDAO certifiedProductDAO,
            final ChplProductNumberUtil chplProductNumberUtil, final ErrorMessageUtil errorMessageUtil,
            final ActivityManager activityManager) {
        this.complaintDAO = complaintDAO;
        this.complaintValidationFactory = complaintValidationFactory;
        this.certifiedProductDAO = certifiedProductDAO;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.errorMessageUtil = errorMessageUtil;
        this.activityManager = activityManager;
    }

    @Transactional
    public Set<KeyValueModel> getComplainantTypes() {
        List<ComplainantTypeDTO> complaintTypes = complaintDAO.getComplainantTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();
        for (ComplainantTypeDTO complaintType : complaintTypes) {
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
        List<Complaint> complaints = new ArrayList<Complaint>();
        List<ComplaintDTO> dtos = complaintDAO.getAllComplaints();
        for (ComplaintDTO dto : dtos) {
            complaints.add(new Complaint(dto));
        }
        return complaints;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).GET_ALL)")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).GET_ALL, filterObject)")
    public List<Complaint> getAllComplaintsBetweenDates(final CertificationBodyDTO acb, final Date startDate,
            final Date endDate) {
        List<Complaint> complaints = new ArrayList<Complaint>();
        List<ComplaintDTO> dtos = complaintDAO.getAllComplaintsBetweenDates(acb.getId(), startDate, endDate);
        for (ComplaintDTO dto : dtos) {
            complaints.add(new Complaint(dto));
        }
        return complaints;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).CREATE, #complaint)")
    public Complaint create(final Complaint complaint)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException {
        ComplaintDTO complaintDTO = new ComplaintDTO(complaint);
        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(runCreateValidations(complaintDTO));
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        ComplaintDTO newComplaint = complaintDAO.create(complaintDTO);

        activityManager.addActivity(ActivityConcept.COMPLAINT, newComplaint.getId(), "Complaint has been created", null,
                newComplaint);
        return new Complaint(newComplaint);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).UPDATE, #complaint)")
    public Complaint update(final Complaint complaint)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException {
        ComplaintDTO complaintDTO = new ComplaintDTO(complaint);
        ComplaintDTO originalFromDB = complaintDAO.getComplaint(complaint.getId());
        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(runUpdateValidations(complaintDTO));
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        ComplaintDTO updatedComplaint = complaintDAO.update(complaintDTO);
        activityManager.addActivity(ActivityConcept.COMPLAINT, updatedComplaint.getId(), "Complaint has been updated",
                originalFromDB, updatedComplaint);

        return new Complaint(updatedComplaint);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).DELETE, #complaintId)")
    public void delete(final Long complaintId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        ComplaintDTO dto = complaintDAO.getComplaint(complaintId);
        if (dto != null) {
            complaintDAO.delete(dto);

            activityManager.addActivity(ActivityConcept.COMPLAINT, dto.getId(), "Complaint has been deleted", dto,
                    null);
        }
    }

    private List<String> runUpdateValidations(ComplaintDTO dto) {
        List<ValidationRule<ComplaintValidationContext>> rules = new ArrayList<ValidationRule<ComplaintValidationContext>>();
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.ACB_CHANGE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.COMPLAINT_TYPE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.RECEIVED_DATE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.ACB_COMPLAINT_ID));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.SUMMARY));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.LISTINGS));
        return runValidations(rules, dto);
    }

    private List<String> runCreateValidations(ComplaintDTO dto) {
        List<ValidationRule<ComplaintValidationContext>> rules = new ArrayList<ValidationRule<ComplaintValidationContext>>();
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.OPEN_STATUS));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.COMPLAINT_TYPE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.RECEIVED_DATE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.ACB_COMPLAINT_ID));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.SUMMARY));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.LISTINGS));
        return runValidations(rules, dto);
    }

    private List<String> runValidations(List<ValidationRule<ComplaintValidationContext>> rules, ComplaintDTO dto) {
        List<String> errorMessages = new ArrayList<String>();
        ComplaintValidationContext context = new ComplaintValidationContext(dto, complaintDAO, certifiedProductDAO,
                errorMessageUtil, chplProductNumberUtil);

        for (ValidationRule<ComplaintValidationContext> rule : rules) {
            if (!rule.isValid(context)) {
                errorMessages.addAll(rule.getMessages());
            }
        }
        return errorMessages;
    }
}
