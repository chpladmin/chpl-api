package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.dto.UcdProcessDTO;

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
                .forEach(ucdProcess -> lookupUcdProcessId(ucdProcess));
        }
    }

    private void lookupUcdProcessId(UcdProcess ucdProcess) {
        if (!StringUtils.isEmpty(ucdProcess.getName())) {
            UcdProcessDTO ucdProcessDto = ucdDao.getByName(ucdProcess.getName());
            if (ucdProcessDto != null) {
                ucdProcess.setId(ucdProcessDto.getId());
            }
        }
    }
}
