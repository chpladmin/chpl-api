package gov.healthit.chpl.validation.listing.reviewer;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.service.CertificationStatusEventsService;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("developerBanComparisonReviewer")
public class DeveloperBanComparisonReviewer implements ComparisonReviewer {
    private CertificationStatusEventsService certStatusEventsService;
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;
    private List<String> certStatusesRequiringOncRole;

    @Autowired
    public DeveloperBanComparisonReviewer(CertificationStatusEventsService certStatusEventsService,
            ResourcePermissions resourcePermissions,
            ErrorMessageUtil msgUtil) {
        this.certStatusEventsService = certStatusEventsService;
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
        this.certStatusesRequiringOncRole = Stream.of(
                CertificationStatusType.SuspendedByOnc.getName(),
                CertificationStatusType.TerminatedByOnc.getName()).collect(Collectors.toList());
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing) {
        List<CertificationStatusEvent> addedCertificationStatusEvents = certStatusEventsService.getAddedCertificationStatusEvents(existingListing, updatedListing);
        List<CertificationStatusEvent> removedCertificationStatusEvents = certStatusEventsService.getRemovedCertificationStatusEvents(existingListing, updatedListing);
        Set<CertificationStatusEvent> modifiedCertificationStatusEvents = Stream.concat(
                addedCertificationStatusEvents.stream(),
                removedCertificationStatusEvents.stream())
                .collect(Collectors.toSet());
        Set<String> modifiedCertificationStatusesRequiringOncRole = modifiedCertificationStatusEvents.stream()
                .filter(cse -> certStatusesRequiringOncRole.contains(cse.getStatus().getName()))
                .map(cse -> cse.getStatus().getName())
                .collect(Collectors.toSet());

        //A non-ONC/ADMIN user should not be allowed to muck with the 'By ONC' status events
        if (!resourcePermissions.isUserRoleOnc()
                && !resourcePermissions.isUserRoleAdmin()
                && !CollectionUtils.isEmpty(modifiedCertificationStatusesRequiringOncRole)) {
            updatedListing.addBusinessErrorMessage(msgUtil.getMessage("listing.certStatusChange.notAllowed",
                    AuthUtil.getUsername(),
                    modifiedCertificationStatusesRequiringOncRole.size() > 1 ? "es" : "",
                    Util.joinListGrammatically(modifiedCertificationStatusesRequiringOncRole.stream().toList())));
        }
    }


}
