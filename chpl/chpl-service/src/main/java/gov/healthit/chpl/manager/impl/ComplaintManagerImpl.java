package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.ComplaintDAO;
import gov.healthit.chpl.dao.ComplaintDTO;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.dto.ComplaintStatusTypeDTO;
import gov.healthit.chpl.dto.ComplaintTypeDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ComplaintManager;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.manager.rules.complaints.ComplaintValidationContext;
import gov.healthit.chpl.manager.rules.complaints.ComplaintValidationFactory;

@Component
public class ComplaintManagerImpl extends SecuredManager implements ComplaintManager {
    private static final String OPEN_STATUS = "Open";

    private ComplaintDAO complaintDAO;
    private ComplaintValidationFactory complaintValidationFactory;

    @Autowired
    public ComplaintManagerImpl(final ComplaintDAO complaintDAO,
            final ComplaintValidationFactory complaintValidationFactory) {
        this.complaintDAO = complaintDAO;
        this.complaintValidationFactory = complaintValidationFactory;
    }

    @Override
    @Transactional
    public Set<KeyValueModel> getComplaintTypes() {
        List<ComplaintTypeDTO> complaintTypes = complaintDAO.getComplaintTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();
        for (ComplaintTypeDTO complaintType : complaintTypes) {
            results.add(new KeyValueModel(complaintType.getId(), complaintType.getName()));
        }
        return results;
    }

    @Override
    @Transactional
    public Set<KeyValueModel> getComplaintStatusTypes() {
        List<ComplaintStatusTypeDTO> complaintStatusTypes = complaintDAO.getComplaintStatusTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();
        for (ComplaintStatusTypeDTO complaintStatusType : complaintStatusTypes) {
            results.add(new KeyValueModel(complaintStatusType.getId(), complaintStatusType.getName()));
        }
        return results;
    }

    @Override
    @Transactional
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

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).CREATE, #complaint)")
    public Complaint create(final Complaint complaint) throws EntityRetrievalException, ValidationException {
        ComplaintDTO complaintDTO = new ComplaintDTO(complaint);
        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(runCreateValidations(complaintDTO));
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        complaintDTO.setComplaintStatusType(getComplaintStatusType(OPEN_STATUS));
        return new Complaint(complaintDAO.create(complaintDTO));
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).UPDATE, #complaint)")
    public Complaint update(final Complaint complaint) throws EntityRetrievalException, ValidationException {
        ComplaintDTO complaintDTO = new ComplaintDTO(complaint);
        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(runUpdateValidations(complaintDTO));
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        return new Complaint(complaintDAO.update(complaintDTO));

    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).DELETE, #complaintId)")
    public void delete(final Long complaintId) throws EntityRetrievalException {
        ComplaintDTO dto = complaintDAO.getComplaint(complaintId);
        if (dto != null) {
            complaintDAO.delete(dto);
        }
    }

    @Override
    public ComplaintStatusTypeDTO getComplaintStatusType(String name) {
        for (ComplaintStatusTypeDTO dto : complaintDAO.getComplaintStatusTypes()) {
            if (dto.getName().equals(name)) {
                return dto;
            }
        }
        return null;
    }

    private List<String> runUpdateValidations(ComplaintDTO dto) {
        List<ValidationRule<ComplaintValidationContext>> rules = new ArrayList<ValidationRule<ComplaintValidationContext>>();
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.ACB_CHANGE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.COMPLAINT_TYPE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.COMPLAINT_STATUS_TYPE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.RECEIVED_DATE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.ACB_COMPLAINT_ID));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.SUMMARY));
        return runValidations(rules, dto);
    }

    private List<String> runCreateValidations(ComplaintDTO dto) {
        List<ValidationRule<ComplaintValidationContext>> rules = new ArrayList<ValidationRule<ComplaintValidationContext>>();
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.OPEN_STATUS));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.COMPLAINT_TYPE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.COMPLAINT_STATUS_TYPE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.RECEIVED_DATE));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.ACB_COMPLAINT_ID));
        rules.add(complaintValidationFactory.getRule(ComplaintValidationFactory.SUMMARY));
        return runValidations(rules, dto);
    }

    private List<String> runValidations(List<ValidationRule<ComplaintValidationContext>> rules, ComplaintDTO dto) {
        List<String> errorMessages = new ArrayList<String>();
        ComplaintValidationContext context = new ComplaintValidationContext(dto, complaintDAO);

        for (ValidationRule<ComplaintValidationContext> rule : rules) {
            if (!rule.isValid(context)) {
                errorMessages.addAll(rule.getMessages());
            }
        }
        return errorMessages;
    }

}
