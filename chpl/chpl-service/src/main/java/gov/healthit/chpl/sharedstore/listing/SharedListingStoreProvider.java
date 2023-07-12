package gov.healthit.chpl.sharedstore.listing;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.sharedstore.SharedStoreDAO;
import gov.healthit.chpl.sharedstore.SharedStoreProvider;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SharedListingStoreProvider extends SharedStoreProvider<Long, CertifiedProductSearchDetails> {
    private ResourcePermissions resourcePermissions;
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public SharedListingStoreProvider( ResourcePermissions resourcePermissions,
            SharedStoreDAO sharedStoreDAO) {
        super(sharedStoreDAO);
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public CertifiedProductSearchDetails get(Long key, Supplier<CertifiedProductSearchDetails> s) {
        CertifiedProductSearchDetails listing = super.get(key, s);
        filterListingDataForUser(listing);
        return listing;
    }

    private void filterListingDataForUser(CertifiedProductSearchDetails listing) {
        if (!canUserViewCertificationEventReasons()) {
            listing.getCertificationEvents().stream()
                .forEach(certEvent -> certEvent.setReason(null));
        }
    }

    private Boolean canUserViewCertificationEventReasons() {
        return AuthUtil.getCurrentUser() != null
                && (resourcePermissions.isUserRoleAcbAdmin()
                        || resourcePermissions.isUserRoleOnc()
                        || resourcePermissions.isUserRoleAdmin());
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
    protected CertifiedProductSearchDetails getFromJson(String json) throws JsonProcessingException {
        return mapper.readValue(json, CertifiedProductSearchDetails.class);
    }

    @Override
    protected Integer getTimeToLive() {
        return SharedListingStoreProvider.UNLIMITED;
    }
}
