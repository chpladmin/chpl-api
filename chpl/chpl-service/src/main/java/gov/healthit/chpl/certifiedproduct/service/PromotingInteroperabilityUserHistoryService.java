package gov.healthit.chpl.certifiedproduct.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.PromotingInteroperabilityUserDAO;
import gov.healthit.chpl.domain.PromotingInteroperabilityUser;

@Component
public class PromotingInteroperabilityUserHistoryService {
    private PromotingInteroperabilityUserDAO piuDao;

    @Autowired
    public PromotingInteroperabilityUserHistoryService(PromotingInteroperabilityUserDAO piuDao) {
        this.piuDao = piuDao;
    }

    public List<PromotingInteroperabilityUser> getPromotingInteroperabilityUserHistory(Long listingId) {
        return piuDao.findByListingId(listingId);
    }
}
