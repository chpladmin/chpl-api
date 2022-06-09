package gov.healthit.chpl.changerequest.search;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ChangeRequestSearchManager {
    private ChangeRequestDAO changeRequestDAO;
    private ResourcePermissions resourcePermissions;

    private ChangeRequestSearchRequestNormalizer normalizer;

    @Autowired
    public ChangeRequestSearchManager(ChangeRequestDAO changeRequestDAO,
            ResourcePermissions resourcePermissions) {
        this.changeRequestDAO = changeRequestDAO;
        this.resourcePermissions = resourcePermissions;
        this.normalizer = new ChangeRequestSearchRequestNormalizer();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).SEARCH)")
    public ChangeRequestSearchResponse searchChangeRequests(ChangeRequestSearchRequest searchRequest) throws EntityRetrievalException {
        normalizer.normalize(searchRequest);

        List<ChangeRequestSearchResult> results = new ArrayList<ChangeRequestSearchResult>();
        if (resourcePermissions.isUserRoleAcbAdmin()) {
            results = changeRequestDAO.getAllForAcbs(resourcePermissions.getAllAcbsForCurrentUser().stream()
                    .map(acb -> acb.getId())
                    .toList());
        } else if (resourcePermissions.isUserRoleDeveloperAdmin()) {
            results = changeRequestDAO.getAllForDevelopers(resourcePermissions.getAllDevelopersForCurrentUser().stream()
                    .map(dev -> dev.getId())
                    .toList());
        } else if (resourcePermissions.isUserRoleOnc() || resourcePermissions.isUserRoleAdmin()) {
            results = changeRequestDAO.getAll();
        }

        return ChangeRequestSearchResponse.builder()
                .results(results)
                .pageNumber(0)
                .pageSize(results.size())
                .recordCount(results.size())
                .build();
    }
}
