package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.ConfirmActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.CreateOrReplaceActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.GetByAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.GetDetailsByIdActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.GetDetailsByIdForActivityActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.UpdateableActionPermissions;

@Component
public class PendingCertifiedProductDomainPermissions extends DomainPermissions {
    public static final String UPDATEABLE = "UPDATEABLE";
    public static final String CONFIRM = "CONFIRM";
    public static final String DELETE = "DELETE";
    public static final String CREATE_OR_REPLACE = "CREATE_OR_REPLACE";
    public static final String GET_BY_ACB = "GET_BY_ACB";
    public static final String GET_ALL = "GET_ALL";
    public static final String GET_DETAILS_BY_ID = "GET_DETAILS_BY_ID";
    public static final String GET_DETAILS_BY_ID_FOR_ACTIVITY = "GET_DETAILS_BY_ID_FOR_ACTIVITY";

    @Autowired
    public PendingCertifiedProductDomainPermissions(final UpdateableActionPermissions updateableActionPermissions,
            final ConfirmActionPermissions confirmActionPermissions,
            final DeleteActionPermissions deleteActionPermissions,
            final CreateOrReplaceActionPermissions createOrReplaceActionPermissions,
            final GetByAcbActionPermissions getByAcbActionPermissions,
            final GetAllActionPermissions getAllActionPermissions,
            final GetDetailsByIdActionPermissions getDetailsByIdActionPermissions,
            final GetDetailsByIdForActivityActionPermissions getDetailsByIdForActivityActionPermissions) {

        getActionPermissions().put(UPDATEABLE, updateableActionPermissions);
        getActionPermissions().put(CONFIRM, confirmActionPermissions);
        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(CREATE_OR_REPLACE, createOrReplaceActionPermissions);
        getActionPermissions().put(GET_BY_ACB, getByAcbActionPermissions);
        getActionPermissions().put(GET_ALL, getAllActionPermissions);
        getActionPermissions().put(GET_DETAILS_BY_ID, getDetailsByIdActionPermissions);
        getActionPermissions().put(GET_DETAILS_BY_ID_FOR_ACTIVITY, getDetailsByIdForActivityActionPermissions);
    }
}
