package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.manager.FuzzyChoicesManager;

@Component
public class QmsStandardNormalizer {
    private QmsStandardDAO qmsStandardDao;
    private FuzzyChoicesManager fuzzyChoicesManager;

    @Autowired
    public QmsStandardNormalizer(QmsStandardDAO qmsStandardDao, FuzzyChoicesManager fuzzyChoicesManager) {
        this.qmsStandardDao = qmsStandardDao;
        this.fuzzyChoicesManager = fuzzyChoicesManager;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getQmsStandards() != null && listing.getQmsStandards().size() > 0) {
            listing.getQmsStandards().stream()
                .forEach(qmsStandard -> populateQmsStandardId(qmsStandard));
            findFuzzyMatchesForUnknownStandards(listing);
        }
    }

    private void populateQmsStandardId(CertifiedProductQmsStandard qmsStandard) {
        if (!StringUtils.isEmpty(qmsStandard.getQmsStandardName())) {
            QmsStandardDTO qmsStdDto =
                    qmsStandardDao.getByName(qmsStandard.getQmsStandardName());
            if (qmsStdDto != null) {
                qmsStandard.setQmsStandardId(qmsStdDto.getId());
            }
        }
    }

    private void findFuzzyMatchesForUnknownStandards(CertifiedProductSearchDetails listing) {
        listing.getQmsStandards().stream()
            .filter(qmsStandard -> qmsStandard.getId() == null)
            .forEach(qmsStandard -> lookForFuzzyMatch(listing, qmsStandard));
    }

    private void lookForFuzzyMatch(CertifiedProductSearchDetails listing, CertifiedProductQmsStandard qmsStandard) {
        String topFuzzyChoice = fuzzyChoicesManager.getTopFuzzyChoice(qmsStandard.getQmsStandardName(), FuzzyType.QMS_STANDARD);
        if (!StringUtils.isEmpty(topFuzzyChoice)) {
            qmsStandard.setUserEnteredQmsStandardName(qmsStandard.getQmsStandardName());
            qmsStandard.setQmsStandardName(topFuzzyChoice);
        }
    }
}
