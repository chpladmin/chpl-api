package gov.healthit.chpl.upload.listing.normalizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

@Component
public class SedNormalizer {
    private TestParticipantNormalizer participantNormalizer;
    private UcdProcessNormalizer ucdProcessNormalizer;

    @Autowired
    public SedNormalizer(TestParticipantNormalizer participantNormalizer,
        UcdProcessNormalizer ucdProcessNormalizer) {
        this.participantNormalizer = participantNormalizer;
        this.ucdProcessNormalizer = ucdProcessNormalizer;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        this.participantNormalizer.normalize(listing);
        this.ucdProcessNormalizer.normalize(listing);
    }
}
