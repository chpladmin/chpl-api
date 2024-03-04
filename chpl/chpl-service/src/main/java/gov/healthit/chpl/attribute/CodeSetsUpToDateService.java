package gov.healthit.chpl.attribute;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.codeset.CertificationResultCodeSet;
import gov.healthit.chpl.codeset.CertificationResultCodeSetDAO;
import gov.healthit.chpl.codeset.CodeSet;
import gov.healthit.chpl.codeset.CodeSetDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.DateUtil;

public class CodeSetsUpToDateService {

    private CertificationResultRules certificationResultRules;
    private CertificationResultCodeSetDAO certificationResultCodeSetDAO;
    private Map<Long, List<CodeSet>> codeSetMaps;

    public CodeSetsUpToDateService(CodeSetDAO codeSetDAO, CertificationResultCodeSetDAO certificationResultCodeSetDAO, CertificationResultRules certificationResultRules) {
        this.codeSetMaps = codeSetDAO.getCodeSetCriteriaMaps();
        this.certificationResultCodeSetDAO = certificationResultCodeSetDAO;
        this.certificationResultRules = certificationResultRules;
    }

    public AttributeUpToDate getAttributeUpToDate(CertificationResult certificationResult, Logger logger) {
        Boolean isCriteriaEligible = isCriteriaEligibleForCodeSets(certificationResult.getCriterion());
        Boolean upToDate = false;
        OptionalLong daysUpdatedEarly = OptionalLong.empty();

        if (isCriteriaEligible) {
            upToDate = areCodeSetsUpToDate(certificationResult);
            if (upToDate) {
                daysUpdatedEarly = getDaysUpdatedEarlyForCriteriaBasedOnCodeSets(certificationResult, logger);
            }
        }

        return AttributeUpToDate.builder()
                .attributeType(AttributeType.CODE_SETS)
                .eligibleForAttribute(isCriteriaEligible)
                .upToDate(upToDate)
                .daysUpdatedEarly(daysUpdatedEarly)
                .criterion(certificationResult.getCriterion())
                .build();
    }

    private OptionalLong getDaysUpdatedEarlyForCriteriaBasedOnCodeSets(CertificationResult certificationResult, Logger logger) {
        //Get the CertificationResultCodeSet using DAO, so that we have the create date
        List<CertificationResultCodeSet> certificationResultCodeSets =
                certificationResultCodeSetDAO.getCodeSetsForCertificationResult(certificationResult.getId());

        OptionalLong daysUpdatedEarly = OptionalLong.empty();
        if (CollectionUtils.isNotEmpty(certificationResultCodeSets)) {
            daysUpdatedEarly = certificationResultCodeSets.stream()
                    .filter(certResultCS -> certResultCS.getCodeSet().getRequiredDay() != null
                            && LocalDate.now().isBefore(certResultCS.getCodeSet().getRequiredDay())
                            && DateUtil.toLocalDate(certResultCS.getCreationDate().getTime()).isBefore(certResultCS.getCodeSet().getRequiredDay()))
                    .mapToLong(certResultFT -> ChronoUnit.DAYS.between(DateUtil.toLocalDate(certResultFT.getCreationDate().getTime()), certResultFT.getCodeSet().getRequiredDay()))
                    .min();

            logger.info("Code Set Check {} - {}", certificationResult.getCriterion().getNumber(), daysUpdatedEarly);
        }
        return daysUpdatedEarly;
    }

    private Boolean areCodeSetsUpToDate(CertificationResult certificationResult) {
        // TODO - Will need to determine for HTI-2 how to correctly handle this.  Possible
        // future state is we will need to make sure the most recent and in the past codes set is
        // attested to.

        // Initially, we will just make sure that the cert result has attested to the same
        // number of code sets as are available for the criteria.
        return CollectionUtils.isNotEmpty(certificationResult.getCodeSets())
                && certificationResult.getCodeSets().size() == codeSetMaps.get(certificationResult.getCriterion().getId()).size();
    }


    private Boolean isCriteriaEligibleForCodeSets(CertificationCriterion criterion) {
        return certificationResultRules.hasCertOption(criterion.getId(), CertificationResultRules.CODE_SET);
    }

}
