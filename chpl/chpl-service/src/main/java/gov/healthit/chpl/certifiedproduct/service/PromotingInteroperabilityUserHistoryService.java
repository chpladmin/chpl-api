package gov.healthit.chpl.certifiedproduct.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.service.comparator.PromotingInteroperabilityComparator;
import gov.healthit.chpl.dao.PromotingInteroperabilityUserDAO;
import gov.healthit.chpl.domain.PromotingInteroperabilityUser;

@Component
public class PromotingInteroperabilityUserHistoryService {
    private PromotingInteroperabilityUserDAO piuDao;
    private PromotingInteroperabilityComparator piComparator;

    @Autowired
    public PromotingInteroperabilityUserHistoryService(PromotingInteroperabilityUserDAO piuDao) {
        this.piuDao = piuDao;
        this.piComparator = new PromotingInteroperabilityComparator();
    }

    public List<PromotingInteroperabilityUser> getPromotingInteroperabilityUserHistory(Long listingId) {
        return piuDao.findByListingId(listingId).stream()
                .sorted(piComparator)
                .collect(Collectors.toList());
    }
}
