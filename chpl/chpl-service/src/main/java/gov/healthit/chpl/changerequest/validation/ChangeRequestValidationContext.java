package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRequestValidationContext {
    private User currentUser;
    private ChangeRequest newChangeRequest;
    private ChangeRequest origChangeRequest;
    private ValidationDAOs validationDAOs;
    private DomainManagers domainManagers;
    private ResourcePermissions resourcePermissions;
    private ValidationUtils validationUtils;
    private ChangeRequestTypeIds changeRequestTypeIds;
    private ChangeRequestStatusIds changeRequestStatusIds;

    public ChangeRequestValidationContext(User user,
            ChangeRequest newChangeRequest,
            ChangeRequest origChangeRequest,
            ResourcePermissions resourcePermissions,
            ValidationUtils validationUtils,
            DeveloperDAO developerDAO,
            ChangeRequestDAO changeRequestDAO,
            ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO,
            ChangeRequestTypeDAO changeRequestTypeDAO,
            AttestationManager attestationManager,
            Long websiteChangeRequestType,
            Long developerDetailsChangeRequestTypeId,
            Long cancelledStatus,
            Long acceptedStatus,
            Long rejectedStatus,
            Long pendingAcbActionStatus,
            Long pendingDeveloperActionStatus) {

        this.currentUser = user;
        this.newChangeRequest = newChangeRequest;
        this.origChangeRequest = origChangeRequest;
        this.resourcePermissions = resourcePermissions;
        this.validationUtils = validationUtils;
        this.validationDAOs = new ValidationDAOs(developerDAO, changeRequestDAO, changeRequestStatusTypeDAO, changeRequestTypeDAO);
        this.domainManagers = new DomainManagers(attestationManager);
        this.changeRequestStatusIds = new ChangeRequestStatusIds(cancelledStatus, acceptedStatus, rejectedStatus, pendingAcbActionStatus, pendingDeveloperActionStatus);
        this.changeRequestTypeIds = new ChangeRequestTypeIds(websiteChangeRequestType, developerDetailsChangeRequestTypeId);
    }

    @Data
    public static class ValidationDAOs {
        private DeveloperDAO developerDAO;
        private ChangeRequestDAO changeRequestDAO;
        private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO;
        private ChangeRequestTypeDAO changeRequestTypeDAO;

        public ValidationDAOs(DeveloperDAO developerDAO,
                ChangeRequestDAO changeRequestDAO,
                ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO,
                ChangeRequestTypeDAO changeRequestTypeDAO) {
            this.developerDAO = developerDAO;
            this.changeRequestDAO = changeRequestDAO;
            this.changeRequestStatusTypeDAO = changeRequestStatusTypeDAO;
            this.changeRequestTypeDAO = changeRequestTypeDAO;
        }
    }

    @Data
    public static class DomainManagers {
        private AttestationManager attestationManager;

        public DomainManagers(AttestationManager attestationManager) {
            this.attestationManager = attestationManager;
        }
    }

    @Data
    public static class ChangeRequestTypeIds {
        private Long websiteChangeRequestType;
        private Long developerDetailsChangeRequestTypeId;

        public ChangeRequestTypeIds(Long websiteChangeRequestType, Long developerDetailsChangeRequestTypeId) {
            this.websiteChangeRequestType = websiteChangeRequestType;
            this.developerDetailsChangeRequestTypeId = developerDetailsChangeRequestTypeId;
        }
    }

    @Data
    public static class ChangeRequestStatusIds {
        private Long cancelledStatus;
        private Long acceptedStatus;
        private Long rejectedStatus;
        private Long pendingAcbActionStatus;
        private Long pendingDeveloperActionStatus;

        public ChangeRequestStatusIds(Long cancelledStatus,
                Long acceptedStatus,
                Long rejectedStatus,
                Long pendingAcbActionStatus,
                Long pendingDeveloperActionStatus) {
            this.cancelledStatus = cancelledStatus;
            this.acceptedStatus = acceptedStatus;
            this.rejectedStatus = rejectedStatus;
            this.pendingAcbActionStatus = pendingAcbActionStatus;
            this.pendingDeveloperActionStatus = pendingDeveloperActionStatus;
        }
    }
}
