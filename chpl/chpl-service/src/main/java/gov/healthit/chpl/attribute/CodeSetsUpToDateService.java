package gov.healthit.chpl.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.codeset.CertificationResultCodeSetDAO;
import gov.healthit.chpl.codeset.CodeSet;
import gov.healthit.chpl.codeset.CodeSetDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.Util;

@Component
public class CodeSetsUpToDateService {

    private CertificationResultRules certificationResultRules;
    private CertificationResultCodeSetDAO certificationResultCodeSetDao;
    private Map<Long, List<CodeSet>> codeSetMaps;

    public CodeSetsUpToDateService(CodeSetDAO codeSetDao,
            CertificationResultCodeSetDAO certificationResultCodeSetDao,
            CertificationResultRules certificationResultRules) {
        this.codeSetMaps = codeSetDao.getCodeSetCriteriaMaps();
        this.certificationResultCodeSetDao = certificationResultCodeSetDao;
        this.certificationResultRules = certificationResultRules;
    }

    public List<CodeSetUpToDate> getAttributeUpToDate(CertificationResult certResult, Logger logger) {
        List<CodeSetUpToDate> codeSetUpToDateReports = new ArrayList<CodeSetUpToDate>();

        Boolean isCriteriaEligible = isCriteriaEligibleForCodeSets(certResult.getCriterion());
        if (isCriteriaEligible) {
            List<CodeSetUpToDate> upToDateReportsForUnattestedCodeSets = getUpToDateReportsForUnattestedCodeSets(certResult, logger);
            if (!CollectionUtils.isEmpty(upToDateReportsForUnattestedCodeSets)) {
                codeSetUpToDateReports.addAll(upToDateReportsForUnattestedCodeSets);
            }
        }

        return codeSetUpToDateReports;
    }

    private Boolean isCriteriaEligibleForCodeSets(CertificationCriterion criterion) {
        List<CodeSet> codeSetsForCriterion = getAllCodeSetsForCriterion(criterion);
        return certificationResultRules.hasCertOption(criterion.getId(), CertificationResultRules.CODE_SET)
                && !CollectionUtils.isEmpty(codeSetsForCriterion);
    }

    private List<CodeSetUpToDate> getUpToDateReportsForUnattestedCodeSets(CertificationResult certResult, Logger logger) {
        logger.info("Checking unattested code sets for " + Util.formatCriteriaNumber(certResult.getCriterion()));
        List<CodeSet> unattestedCodeSets = getUnattestedToCodeSets(certResult);
        logger.info("There are " + unattestedCodeSets.size() + " unattested code sets for " + Util.formatCriteriaNumber(certResult.getCriterion()));

        List<CodeSetUpToDate> codeSetUpToDateReports = new ArrayList<CodeSetUpToDate>();
        if (CollectionUtils.isNotEmpty(unattestedCodeSets)) {
            unattestedCodeSets.stream()
                    .peek(cs -> logger.info("Checking unattested code set " + cs.getName()))
                    .filter(cs -> cs.getRequiredDay() != null)
                    .peek(cs -> logger.info("Unattested code set " + cs.getName() + " is required but not found"))
                    .map(cs -> CodeSetUpToDate.builder()
                            .criterion(certResult.getCriterion())
                            .eligibleForAttribute(true)
                            .expiringButPresent(false)
                            .requiredButNotPresent(true)
                            .codeSet(cs)
                            .build())
                    .forEach(csUpToDate -> codeSetUpToDateReports.add(csUpToDate));
        }
        return codeSetUpToDateReports;
    }

    private List<CodeSet> getUnattestedToCodeSets(CertificationResult certResult) {
        return getAllCodeSetsForCriterion(certResult.getCriterion()).stream()
                .filter(codeSet -> !isCodeSetInList(codeSet, certResult.getCodeSets().stream().map(crcs -> crcs.getCodeSet()).toList()))
                .toList();
    }

    private Boolean isCodeSetInList(CodeSet codeSetToCheck, List<CodeSet> codeSets) {
        return codeSets.stream()
                .filter(cs -> cs.getId().equals(codeSetToCheck.getId()))
                .findAny()
                .isPresent();
    }

    private Boolean areCodeSetsUpToDate(CertificationResult certificationResult) {
        // TODO - Will need to determine for HTI-2 how to correctly handle this.  Possible
        // future state is we will need to make sure the most recent and in the past codes set is
        // attested to.

        // Initially, we will just make sure that the cert result has attested to the same
        // number of code sets as are available for the criteria.
        return (CollectionUtils.isNotEmpty(certificationResult.getCodeSets())
                && certificationResult.getCodeSets().size() == codeSetMaps.get(certificationResult.getCriterion().getId()).size())
                || CollectionUtils.isEmpty(getAllCodeSetsForCriterion(certificationResult.getCriterion()));
    }

    private List<CodeSet> getAllCodeSetsForCriterion(CertificationCriterion criterion) {
        if (codeSetMaps.containsKey(criterion.getId())) {
            return codeSetMaps.get(criterion.getId());
        } else {
            return List.of();
        }
    }

}
