package gov.healthit.chpl.certifiedproduct.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.MeaningfulUseUserDAO;
import gov.healthit.chpl.domain.PromotingInteroperability;
import gov.healthit.chpl.dto.MeaningfulUseUserDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component
public class MeaningfulUseUserHistoryService {
    private MeaningfulUseUserDAO muuDao;

    @Autowired
    public MeaningfulUseUserHistoryService(MeaningfulUseUserDAO muuDao) {
        this.muuDao = muuDao;
    }

    public List<PromotingInteroperability> getMeaningfulUseUserHistory(Long certifiedProductId) throws EntityRetrievalException {

        List<PromotingInteroperability> muuHistory = new ArrayList<PromotingInteroperability>();
        List<MeaningfulUseUserDTO> muuDtos = muuDao.findByCertifiedProductId(certifiedProductId);

        for (MeaningfulUseUserDTO muuDto : muuDtos) {
            PromotingInteroperability muu = new PromotingInteroperability(muuDto);
            muuHistory.add(muu);
        }
        return muuHistory;
    }

}
