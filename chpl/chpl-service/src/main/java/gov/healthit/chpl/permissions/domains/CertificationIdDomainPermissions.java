package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.certificationId.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationId.GetAllWithProductsActionPermissions;

@Component
public class CertificationIdDomainPermissions extends DomainPermissions {
    public static final String GET_ALL = "GET_ALL";
    public static final String GET_ALL_WITH_PRODUCTS = "GET_ALL_WITH_PRODUCTS";

    @Autowired
    public CertificationIdDomainPermissions(
            @Qualifier("certificationIdsGetAllActionPermissions") GetAllActionPermissions getAllActionPermissions,
            @Qualifier("certificationIdsGetAllWithProductsActionPermissions") GetAllWithProductsActionPermissions getAllWithProductsActionPermissions) {

        getActionPermissions().put(GET_ALL, getAllActionPermissions);
        getActionPermissions().put(GET_ALL_WITH_PRODUCTS, getAllWithProductsActionPermissions);
    }
}
