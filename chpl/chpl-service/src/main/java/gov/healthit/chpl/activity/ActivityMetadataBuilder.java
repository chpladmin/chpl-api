package gov.healthit.chpl.activity;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.AnnouncementActivityMetadata;
import gov.healthit.chpl.domain.activity.AnnualReportActivityMetadata;
import gov.healthit.chpl.domain.activity.ApiKeyManagementActivityMetadata;
import gov.healthit.chpl.domain.activity.CertificationBodyActivityMetadata;
import gov.healthit.chpl.domain.activity.ChangeRequestActivityMetadata;
import gov.healthit.chpl.domain.activity.ComplaintActivityMetadata;
import gov.healthit.chpl.domain.activity.CorrectiveActionPlanActivityMetadata;
import gov.healthit.chpl.domain.activity.DeveloperActivityMetadata;
import gov.healthit.chpl.domain.activity.ListingActivityMetadata;
import gov.healthit.chpl.domain.activity.PendingSurveillanceActivityMetadata;
import gov.healthit.chpl.domain.activity.ProductActivityMetadata;
import gov.healthit.chpl.domain.activity.QuarterlyReportActivityMetadata;
import gov.healthit.chpl.domain.activity.TestingLabActivityMetadata;
import gov.healthit.chpl.domain.activity.UserMaintenanceActivityMetadata;
import gov.healthit.chpl.domain.activity.VersionActivityMetadata;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.auth.CognitoUserService;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class ActivityMetadataBuilder {

    private CognitoUserService cognitoUserService;
    private UserDAO userDAO;

    public ActivityMetadataBuilder(CognitoUserService cognitoUserService, UserDAO userDAO) {
        this.cognitoUserService = cognitoUserService;
        this.userDAO = userDAO;
    }

   public ActivityMetadata build(final ActivityDTO dto) {
        ActivityMetadata metadata = createMetadataObject(dto);
        if (metadata != null) {
            addGenericMetadata(dto, metadata);
            addConceptSpecificMetadata(dto, metadata);
        }
        return metadata;
    }

    protected abstract void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata);

    protected void addGenericMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        metadata.setId(dto.getId());
        metadata.setDate(dto.getActivityDate());
        metadata.setObjectId(dto.getActivityObjectId());
        metadata.setConcept(dto.getConcept());
        metadata.setResponsibleUser(getUserFromActivityDTO(dto));
        metadata.setDescription(dto.getDescription());
    }

    private ActivityMetadata createMetadataObject(ActivityDTO dto) {
        ActivityMetadata metadata = null;
        switch (dto.getConcept()) {
        case CERTIFIED_PRODUCT:
            metadata = new ListingActivityMetadata();
            break;
        case DEVELOPER:
            metadata = new DeveloperActivityMetadata();
            break;
        case PRODUCT:
            metadata = new ProductActivityMetadata();
            break;
        case VERSION:
            metadata = new VersionActivityMetadata();
            break;
        case CERTIFICATION_BODY:
            metadata = new CertificationBodyActivityMetadata();
            break;
        case TESTING_LAB:
            metadata = new TestingLabActivityMetadata();
            break;
        case USER:
            metadata = new UserMaintenanceActivityMetadata();
            break;
        case ANNOUNCEMENT:
            metadata = new AnnouncementActivityMetadata();
            break;
        case CORRECTIVE_ACTION_PLAN:
            metadata = new CorrectiveActionPlanActivityMetadata();
            break;
        case PENDING_SURVEILLANCE:
            metadata = new PendingSurveillanceActivityMetadata();
            break;
        case COMPLAINT:
            metadata = new ComplaintActivityMetadata();
            break;
        case QUARTERLY_REPORT:
        case QUARTERLY_REPORT_LISTING:
            metadata = new QuarterlyReportActivityMetadata();
            break;
        case ANNUAL_REPORT:
            metadata = new AnnualReportActivityMetadata();
            break;
        case CHANGE_REQUEST:
            metadata = new ChangeRequestActivityMetadata();
            break;
        case API_KEY:
            metadata = new ApiKeyManagementActivityMetadata();
            break;
        default:
            break;
        }
        return metadata;
    }

    protected User getUserFromActivityDTO(ActivityDTO activityDTO) {
            User currentUser = null;
            if (activityDTO.getLastModifiedUser() != null) {
                try {
                    currentUser = userDAO.getById(activityDTO.getLastModifiedUser(), true).toDomain();
                } catch (UserRetrievalException e) {
                    LOGGER.error("Could not retreive user with ID: {}", activityDTO.getLastModifiedUser(), e);
                }
            } else if (activityDTO.getLastModifiedSsoUser() != null) {
                try {
                    currentUser = cognitoUserService.getUserInfo(activityDTO.getLastModifiedSsoUser());
                } catch (UserRetrievalException e) {
                    LOGGER.error("Could not retreive user with ID: {}", activityDTO.getLastModifiedSsoUser(), e);
                }
            }
            return currentUser;
    }

}
