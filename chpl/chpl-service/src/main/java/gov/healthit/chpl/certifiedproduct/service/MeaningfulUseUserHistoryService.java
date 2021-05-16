package gov.healthit.chpl.certifiedproduct.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.MeaningfulUseUserDAO;
import gov.healthit.chpl.domain.MeaningfulUseUser;
import gov.healthit.chpl.dto.MeaningfulUseUserDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component
public class MeaningfulUseUserHistoryService {
    private MeaningfulUseUserDAO muuDao;

    @Autowired
    public MeaningfulUseUserHistoryService(MeaningfulUseUserDAO muuDao) {
        this.muuDao = muuDao;
    }

    public List<MeaningfulUseUser> getMeaningfulUseUserHistory(Long certifiedProductId) throws EntityRetrievalException {

        List<MeaningfulUseUser> muuHistory = new ArrayList<MeaningfulUseUser>();
        List<MeaningfulUseUserDTO> muuDtos = muuDao.findByCertifiedProductId(certifiedProductId);

        for (MeaningfulUseUserDTO muuDto : muuDtos) {
            MeaningfulUseUser muu = new MeaningfulUseUser(muuDto);
            muuHistory.add(muu);
        }
        return muuHistory;
    }

}
