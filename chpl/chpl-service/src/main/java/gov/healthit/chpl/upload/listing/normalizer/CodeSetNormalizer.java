package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.codeset.CertificationResultCodeSet;
import gov.healthit.chpl.codeset.CodeSet;
import gov.healthit.chpl.codeset.CodeSetDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

@Component
public class CodeSetNormalizer implements CertificationResultLevelNormalizer {
    private CodeSetDAO codeSetDao;

    @Autowired
    public CodeSetNormalizer(CodeSetDAO codeSetDao) {
        this.codeSetDao = codeSetDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            clearDataForUnattestedCriteria(listing);

            listing.getCertificationResults().stream()
                .forEach(certResult -> fillInCodeSetsData(listing, certResult));
        }
    }

    private void clearDataForUnattestedCriteria(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> (certResult.getSuccess() == null || BooleanUtils.isFalse(certResult.getSuccess()))
                    && certResult.getCodeSets() != null
                    && certResult.getCodeSets().size() > 0)
            .forEach(unattestedCertResult -> unattestedCertResult.getCodeSets().clear());
    }

    private void fillInCodeSetsData(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        populateCodeSets(listing, certResult, certResult.getCodeSets());
    }

    private void populateCodeSets(CertifiedProductSearchDetails listing, CertificationResult certResult, List<CertificationResultCodeSet> codeSets) {
        if (codeSets != null && codeSets.size() > 0) {
            codeSets.stream()
                .filter(codeSet -> codeSet.getId() == null)
                .forEach(codeSet -> populateCodeSet(listing, certResult, codeSet));
        }
    }

    private void populateCodeSet(CertifiedProductSearchDetails listing, CertificationResult certResult, CertificationResultCodeSet codeSet) {
        if (!StringUtils.isEmpty(codeSet.getCodeSet().getUserEnteredName())) {
            CodeSet foundCodeSet =
                    getCodeSet(codeSet.getCodeSet().getUserEnteredName(), certResult.getCriterion().getId());
            if (foundCodeSet != null) {
                codeSet.setCodeSet(foundCodeSet);
            }
        }
    }

    private CodeSet getCodeSet(String codeSetText, Long criterionId) {
        Map<Long, List<CodeSet>> codeSetMappings = codeSetDao.getCodeSetCriteriaMaps();
        if (!codeSetMappings.containsKey(criterionId)) {
            return null;
        }
        List<CodeSet> codeSetsForCriterion = codeSetMappings.get(criterionId);
        Optional<CodeSet> codeSetOpt = codeSetsForCriterion.stream()
            .filter(codeSet -> codeSet.getName().equalsIgnoreCase(codeSetText))
            .findAny();
        return codeSetOpt.isPresent() ? codeSetOpt.get() : null;
    }
}
