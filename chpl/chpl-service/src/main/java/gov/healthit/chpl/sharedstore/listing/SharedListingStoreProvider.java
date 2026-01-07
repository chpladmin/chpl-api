package gov.healthit.chpl.sharedstore.listing;

import java.util.function.Supplier;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.sharedstore.SharedStoreDAO;
import gov.healthit.chpl.sharedstore.SharedStoreProvider;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;
import tools.jackson.core.JacksonException;

@Component
@Log4j2
public class SharedListingStoreProvider extends SharedStoreProvider<Long, CertifiedProductSearchDetails> {
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @Autowired
    public SharedListingStoreProvider(ResourcePermissionsFactory resourcePermissionsFactory,
            SharedStoreDAO sharedStoreDAO) {
        super(sharedStoreDAO);
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    @Override
    public CertifiedProductSearchDetails get(Long key, Supplier<CertifiedProductSearchDetails> s) {
        CertifiedProductSearchDetails listing = super.get(key, s);
        if (listing != null) {
            filterListingDataForUser(listing);
        }
        return listing;
    }

    private void filterListingDataForUser(CertifiedProductSearchDetails listing) {
        if (!canUserViewCertificationEventReasons() && !CollectionUtils.isEmpty(listing.getCertificationEvents())) {
            listing.getCertificationEvents().stream()
                .forEach(certEvent -> certEvent.setReason(null));
        }
    }

    private Boolean canUserViewCertificationEventReasons() {
        return AuthUtil.getCurrentUser() != null
                && (resourcePermissionsFactory.get().isUserRoleAcbAdmin()
                        || resourcePermissionsFactory.get().isUserRoleOnc()
                        || resourcePermissionsFactory.get().isUserRoleAdmin());
    }

    @Override
    protected String getDomain() {
        return CertifiedProductSearchDetails.class.getName();
    }

    @Override
    protected Class<CertifiedProductSearchDetails> getClazz() {
        return CertifiedProductSearchDetails.class;
    }

    @Override
    protected CertifiedProductSearchDetails getFromJson(String json) throws JacksonException {
        return getMapper().readValue(json, CertifiedProductSearchDetails.class);
    }

    @Override
    protected Integer getTimeToLive() {
        return SharedListingStoreProvider.UNLIMITED;
    }
}
