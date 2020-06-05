package gov.healthit.chpl.permissions.domains;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductSummaryDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.permissions.ResourcePermissions;

public abstract class ActionPermissions {
    @Autowired
    private ResourcePermissions resourcePermissions;

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO;

    public abstract boolean hasAccess();

    public abstract boolean hasAccess(Object obj);

    public boolean isAcbValidForCurrentUser(final Long acbId) {
        List<CertificationBodyDTO> acbs = resourcePermissions.getAllAcbsForCurrentUser();
        for (CertificationBodyDTO dto : acbs) {
            if (dto.getId().equals(acbId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAtlValidForCurrentUser(final Long atlId) {
        List<TestingLabDTO> atls = resourcePermissions.getAllAtlsForCurrentUser();
        for (TestingLabDTO dto : atls) {
            if (dto.getId().equals(atlId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDeveloperValidForCurrentUser(final Long developerId) {
        List<DeveloperDTO> developers = resourcePermissions.getAllDevelopersForCurrentUser();
        for (DeveloperDTO dto : developers) {
            if (dto.getId().equals(developerId)) {
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public boolean doesCurrentUserHaveAccessToAllOfDevelopersListings(Long developerId,
            List<CertificationStatusType> listingStatuses) {
        List<CertifiedProductSummaryDTO> cpDtos = certifiedProductDAO.findListingSummariesByDeveloperId(developerId);
        return !cpDtos.stream().filter(cpDto ->
                !isAcbValidForCurrentUser(cpDto.getAcb().getId())
                && isInStatuses(cpDto.getCertificationStatus(), listingStatuses))
                .findAny().isPresent();
    }

    @Transactional(readOnly = true)
    public boolean doesCurrentUserHaveAccessToAllOfProductListings(Long productId) {
        List<CertifiedProductDetailsDTO> cpDtos = certifiedProductDAO.getDetailsByProductId(productId);
        for (CertifiedProductDetailsDTO cpDto : cpDtos) {
            if (!isAcbValidForCurrentUser(cpDto.getCertificationBodyId())) {
                return false;
            }
        }
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isCurrentAcbUserAssociatedWithDeveloper(final Long developerId) {
        List<CertificationBody> developerAcbs = developerCertificationBodyMapDAO
                .getCertificationBodiesForDeveloper(developerId);
        List<CertificationBody> userAcbs = resourcePermissions.getAllAcbsForCurrentUser().stream()
                .map(acb -> new CertificationBody(acb))
                .collect(Collectors.<CertificationBody>toList());

        return developerAcbs.stream()
                .anyMatch(developerAcb -> userAcbs.stream()
                        .anyMatch(userAcb -> userAcb.getId().equals(developerAcb.getId())));
    }

    private boolean isInStatuses(String certStatusName, List<CertificationStatusType> listingStatuses) {
        return listingStatuses.stream()
        .filter(status -> status.getName().equals(certStatusName)).findAny().isPresent();
    }

    public ResourcePermissions getResourcePermissions() {
        return resourcePermissions;
    }
}
