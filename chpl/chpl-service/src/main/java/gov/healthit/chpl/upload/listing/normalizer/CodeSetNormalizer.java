package gov.healthit.chpl.upload.listing.normalizer;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private static final String[] ACCEPTED_DATE_FORMATS = {
            "yyyyMM", "MM-yyyy", "MM-yy", "MMM-yy", "MMM yyyy", "MMMM yyyy"
    };
    private DateTimeFormatter codeSetFormatter;
    private List<DateTimeFormatter> formatters;

    private CodeSetDAO codeSetDao;

    @Autowired
    public CodeSetNormalizer(CodeSetDAO codeSetDao) {
        this.codeSetDao = codeSetDao;
        codeSetFormatter = DateTimeFormatter.ofPattern(CodeSet.CODE_SET_DATE_FORMAT);
        formatters = new ArrayList<DateTimeFormatter>();
        for (String format : ACCEPTED_DATE_FORMATS) {
            formatters.add(DateTimeFormatter.ofPattern(format));
        }
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
            .filter(codeSet -> matchesCodeSetName(codeSet, codeSetText))
            .findAny();
        return codeSetOpt.isPresent() ? codeSetOpt.get() : null;
    }

    private boolean matchesCodeSetName(CodeSet codeSet, String codeSetText) {
        if (codeSet.getName().equalsIgnoreCase(codeSetText)) {
            return true;
        }
        //try parsing user-entered text in other date formats
        YearMonth userEnteredCodeSetDate = parseAsYearMonth(codeSetText);
        if (userEnteredCodeSetDate != null) {
            String formattedUserEnteredCodeSet = userEnteredCodeSetDate.format(codeSetFormatter);
            return codeSet.getName().equalsIgnoreCase(formattedUserEnteredCodeSet);
        }
        return false;
    }

    private YearMonth parseAsYearMonth(String value) {
        YearMonth parsedYearMonth = null;
        for (DateTimeFormatter formatter : formatters) {
            try {
                YearMonth yearMonth = YearMonth.parse(value, formatter);
                if (yearMonth != null && parsedYearMonth == null) {
                    parsedYearMonth = yearMonth;
                }
            } catch (Exception ignore) { }
        }
        return parsedYearMonth;
    }
}
