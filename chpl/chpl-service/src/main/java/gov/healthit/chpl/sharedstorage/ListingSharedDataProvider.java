package gov.healthit.chpl.sharedstorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

@Component
public class ListingSharedDataProvider extends SharedDataProvider<Long, CertifiedProductSearchDetails> {
    private ObjectMapper mapper = new ObjectMapper();
    private Integer timeToLiveInHours;

    @Autowired
    public ListingSharedDataProvider(SharedDataDAO sharedDataDAO,
          @Value("${sharedData.timeToLive.listings}") Integer timeToLiveInHours) {
        super(sharedDataDAO);

        this.timeToLiveInHours = timeToLiveInHours;
    }

    @Override
    public String getDomain() {
        return CertifiedProductSearchDetails.class.getName();
    }

    @Override
    public Class<CertifiedProductSearchDetails> getClazz() {
        return CertifiedProductSearchDetails.class;
    }

    @Override
    public CertifiedProductSearchDetails getFromJson(String json) {

        try {
            return mapper.readValue(json, CertifiedProductSearchDetails.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Integer getTimeToLive() {
        return timeToLiveInHours;
    }


}
