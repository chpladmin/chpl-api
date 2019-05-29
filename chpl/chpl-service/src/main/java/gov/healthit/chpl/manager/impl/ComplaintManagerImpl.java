package gov.healthit.chpl.manager.impl;

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
import gov.healthit.chpl.dto.ComplaintStatusTypeDTO;
import gov.healthit.chpl.dto.ComplaintTypeDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ComplaintManager;

@Component
public class ComplaintManagerImpl extends SecuredManager implements ComplaintManager {
    private ComplaintDAO complaintDAO;

    @Autowired
    public ComplaintManagerImpl(final ComplaintDAO complaintDAO) {
        this.complaintDAO = complaintDAO;
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
    public List<ComplaintDTO> getAllComplaints() {
        return complaintDAO.getAllComplaints();
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).CREATE, #complaintDTO)")
    public ComplaintDTO create(ComplaintDTO complaintDTO) {
        return complaintDAO.create(complaintDTO);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).UPDATE, #complaintDTO)")
    public ComplaintDTO update(ComplaintDTO complaintDTO) throws EntityRetrievalException {
        return complaintDAO.update(complaintDTO);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).DELETE, #complaintDTO)")
    public void delete(ComplaintDTO complaintDTO) throws EntityRetrievalException {
        complaintDAO.delete(complaintDTO);
    }
}
