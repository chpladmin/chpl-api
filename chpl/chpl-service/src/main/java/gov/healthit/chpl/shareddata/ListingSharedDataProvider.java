package gov.healthit.chpl.shareddata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
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
    public CertifiedProductSearchDetails getFromJson(String json) throws JsonProcessingException {
        return mapper.readValue(json, CertifiedProductSearchDetails.class);
    }

    @Override
    public Integer getTimeToLive() {
        return timeToLiveInHours;
    }


}
