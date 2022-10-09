package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.ucdProcess.UcdProcess;
import gov.healthit.chpl.ucdProcess.UcdProcessDAO;

@Component
public class UcdProcessNormalizer {
    private UcdProcessDAO ucdDao;

    @Autowired
    public UcdProcessNormalizer(UcdProcessDAO ucdDao) {
        this.ucdDao = ucdDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getSed() != null && listing.getSed().getUcdProcesses() != null
                && listing.getSed().getUcdProcesses().size() > 0) {
            listing.getSed().getUcdProcesses().stream()
                .forEach(ucdProcess -> populateUcdProcessId(ucdProcess));
        }
    }

    private void populateUcdProcessId(CertifiedProductUcdProcess ucdProcess) {
        //TODO: Add some fuzzy matching
        if (!StringUtils.isEmpty(ucdProcess.getName())) {
            UcdProcess foundUcdProcess = ucdDao.getByName(ucdProcess.getName());
            if (foundUcdProcess != null) {
                ucdProcess.setId(foundUcdProcess.getId());
            }
        }
    }
}
