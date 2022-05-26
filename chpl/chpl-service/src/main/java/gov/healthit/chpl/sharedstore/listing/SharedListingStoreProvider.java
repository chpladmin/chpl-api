package gov.healthit.chpl.sharedstore.listing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.sharedstore.SharedStoreDAO;
import gov.healthit.chpl.sharedstore.SharedStoreProvider;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SharedListingStoreProvider extends SharedStoreProvider<Long, CertifiedProductSearchDetails> {
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public SharedListingStoreProvider(SharedStoreDAO sharedStoreDAO) {
        super(sharedStoreDAO);
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
