package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.attestation.manager.AttestationManager;
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
    private ChangeRequest newChangeRequest;
    private ChangeRequest origChangeRequest;
    private ValidationDAOs validationDAOs;
    private DomainManagers domainManagers;
    private ResourcePermissions resourcePermissions;
    private ValidationUtils validationUtils;
    private ChangeRequestTypeIds changeRequestTypeIds;
    private ChangeRequestStatusIds changeRequestStatusIds;

    public ChangeRequestValidationContext(ChangeRequest newChangeRequest,
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

        this.newChangeRequest = newChangeRequest;
        this.origChangeRequest = origChangeRequest;
        this.resourcePermissions = resourcePermissions;
        this.validationUtils = validationUtils;
        this.validationDAOs = new ValidationDAOs(developerDAO, changeRequestDAO, changeRequestStatusTypeDAO, changeRequestTypeDAO);
        this.domainManagers = new DomainManagers(attestationManager);
        this.changeRequestStatusIds = new ChangeRequestStatusIds(cancelledStatus, acceptedStatus, rejectedStatus, pendingAcbActionStatus, pendingDeveloperActionStatus);
        this.changeRequestTypeIds = new ChangeRequestTypeIds(websiteChangeRequestType, developerDetailsChangeRequestTypeId);
    }


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

        public DeveloperDAO getDeveloperDAO() {
            return developerDAO;
        }

        public ChangeRequestDAO getChangeRequestDAO() {
            return changeRequestDAO;
        }

        public ChangeRequestStatusTypeDAO getChangeRequestStatusTypeDAO() {
            return changeRequestStatusTypeDAO;
        }

        public ChangeRequestTypeDAO getChangeRequestTypeDAO() {
            return changeRequestTypeDAO;
        }

    }

    public static class DomainManagers {
        public DomainManagers(AttestationManager attestationManager) {
            this.attestationManager = attestationManager;
        }

        private AttestationManager attestationManager;

        public AttestationManager getAttestationManager() {
            return attestationManager;
        }
    }

    public static class ChangeRequestTypeIds {
        private Long websiteChangeRequestType;
        private Long developerDetailsChangeRequestTypeId;

        public ChangeRequestTypeIds(Long websiteChangeRequestType, Long developerDetailsChangeRequestTypeId) {
            this.websiteChangeRequestType = websiteChangeRequestType;
            this.developerDetailsChangeRequestTypeId = developerDetailsChangeRequestTypeId;
        }

        public Long getWebsiteChangeRequestType() {
            return websiteChangeRequestType;
        }

        public Long getDeveloperDetailsChangeRequestTypeId() {
            return developerDetailsChangeRequestTypeId;
        }
    }

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
            this.pendingDeveloperActionStatus = pendingAcbActionStatus;
        }

        public Long getCancelledStatus() {
            return cancelledStatus;
        }

        public Long getAcceptedStatus() {
            return acceptedStatus;
        }

        public Long getRejectedStatus() {
            return rejectedStatus;
        }

        public Long getPendingAcbActionStatus() {
            return pendingAcbActionStatus;
        }

        public Long getPendingDeveloperActionStatus() {
            return pendingDeveloperActionStatus;
        }
    }
}
