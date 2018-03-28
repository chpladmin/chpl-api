package gov.healthit.chpl.validation.certifiedProduct;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.util.CertificationResultRules;

@Component("certifiedProduct2014Validator")
public class CertifiedProduct2014Validator extends CertifiedProductValidatorImpl {
    private static final String[] G1_COMPLEMENTARY_CERTS = {
            "170.314 (a)(1)", "170.314 (a)(3)", "170.314 (a)(4)", "170.314 (a)(5)", "170.314 (a)(6)", "170.314 (a)(7)",
            "170.314 (a)(9)", "170.314 (a)(11)", "170.314 (a)(12)", "170.314 (a)(13)", "170.314 (a)(14)",
            "170.314 (a)(15)", "170.314 (a)(18)", "170.314 (a)(19)", "170.314 (a)(20)", "170.314 (b)(2)",
            "170.314 (b)(3)", "170.314 (b)(4)", "170.314 (e)(1)"
    };

    private static final String[] G2_COMPLEMENTARY_CERTS = {
            "170.314 (a)(1)", "170.314 (a)(3)", "170.314 (a)(4)", "170.314 (a)(5)", "170.314 (a)(6)", "170.314 (a)(7)",
            "170.314 (a)(9)", "170.314 (a)(11)", "170.314 (a)(12)", "170.314 (a)(13)", "170.314 (a)(14)",
            "170.314 (a)(15)", "170.314 (a)(18)", "170.314 (a)(19)", "170.314 (a)(20)", "170.314 (b)(2)",
            "170.314 (b)(3)", "170.314 (b)(4)", "170.314 (e)(1)"
    };

    private static final String[] G3_COMPLEMENTARY_CERTS = {
            "170.314 (a)(1)", "170.314 (a)(2)", "170.314 (a)(6)", "170.314 (a)(7)", "170.314 (a)(8)", "170.314 (a)(16)",
            "170.314 (a)(18)", "170.314 (a)(19)", "170.314 (a)(20)", "170.314 (b)(3)", "170.314 (b)(4)",
            "170.314 (b)(9)"
    };

    private static final String[] CQM_REQUIRED_CERTS = {
            "170.314 (c)(1)", "170.314 (c)(2)", "170.314 (c)(3)"
    };

    public String[] getG1ComplimentaryCerts() {
        return G1_COMPLEMENTARY_CERTS;
    }

    public String[] getG2ComplimentaryCerts() {
        return G2_COMPLEMENTARY_CERTS;
    }

    public boolean checkB1B2B8H1(final Object product){
        // (b)(1), (b)(2)**
        // (in replacement for (b)(1) and (b)(2) -
        // (b)(1) and (b)(8), OR
        // (b)(8) and (h)(1), OR
        // (b)(1) and (b)(2) and (b)(8), OR
        // (b)(1) and (b)(2) and (h)(1), OR
        // (b)(1) and (b)(2) and (b)(8) and (h)(1)"
        boolean hasB1 = false;
        boolean hasB2 = false;
        boolean hasB8 = false;
        boolean hasH1 = false;
        if (product instanceof PendingCertifiedProductDTO) {
            PendingCertifiedProductDTO productCasted = (PendingCertifiedProductDTO) product;
            for (PendingCertificationResultDTO certCriteria : productCasted.getCertificationCriterion()){
                if (certCriteria.getNumber().equals("170.314 (b)(1)") && certCriteria.getMeetsCriteria()) {
                    hasB1 = true;
                }
                if (certCriteria.getNumber().equals("170.314 (b)(2)") && certCriteria.getMeetsCriteria()) {
                    hasB2 = true;
                }
                if (certCriteria.getNumber().equals("170.314 (b)(8)") && certCriteria.getMeetsCriteria()) {
                    hasB8 = true;
                }
                if (certCriteria.getNumber().equals("170.314 (h)(1)") && certCriteria.getMeetsCriteria()) {
                    hasH1 = true;
                }
            }
            if (!hasB1 && !hasB2) {
                if (!hasB1 && !hasB8) {
                    if (!hasB8 && !hasH1) {
                        productCasted.getErrorMessages()
                        .add("An allowed combination of (b)(1), (b)(2), (b)(8), and (h)(1) was not found.");
                        return false;
                    }
                }
            }
        } else {
            CertifiedProductSearchDetails productCasted = (CertifiedProductSearchDetails) product;
            for (CertificationResult certCriteria : productCasted.getCertificationResults()) {
                if (certCriteria.getNumber().equals("170.314 (b)(1)") && certCriteria.isSuccess()) {
                    hasB1 = true;
                }
                if (certCriteria.getNumber().equals("170.314 (b)(2)") && certCriteria.isSuccess()) {
                    hasB2 = true;
                }
                if (certCriteria.getNumber().equals("170.314 (b)(8)") && certCriteria.isSuccess()) {
                    hasB8 = true;
                }
                if (certCriteria.getNumber().equals("170.314 (h)(1)") && certCriteria.isSuccess()) {
                    hasH1 = true;
                }
            }
            if (!hasB1 && !hasB2) {
                if (!hasB1 && !hasB8) {
                    if (!hasB8 && !hasH1) {
                        productCasted.getErrorMessages()
                        .add("An allowed combination of (b)(1), (b)(2), (b)(8), and (h)(1) was not found.");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean checkA1OrA18A19A20(final Object product){
        boolean hasA1 = false;
        boolean hasA18 = false;
        boolean hasA19 = false;
        boolean hasA20 = false;
        if (product instanceof PendingCertifiedProductDTO) {
            PendingCertifiedProductDTO productCasted = (PendingCertifiedProductDTO) product;
            for (PendingCertificationResultDTO certCriteria : productCasted.getCertificationCriterion()) {
                if (certCriteria.getNumber().equals("170.314 (a)(1)") && certCriteria.getMeetsCriteria()) {
                    hasA1 = true;
                }
                if (certCriteria.getNumber().equals("170.314 (a)(18)") && certCriteria.getMeetsCriteria()) {
                    hasA18 = true;
                }
                if (certCriteria.getNumber().equals("170.314 (a)(19)") && certCriteria.getMeetsCriteria()) {
                    hasA19 = true;
                }
                if (certCriteria.getNumber().equals("170.314 (a)(20)") && certCriteria.getMeetsCriteria()) {
                    hasA20 = true;
                }
            }
            if (!hasA1) {
                if (!hasA18 || !hasA19 || !hasA20) {
                    productCasted.getErrorMessages()
                    .add("Neither (a)(1) nor the combination of (a)(18), (a)(19), and (a)(20) were found.");
                    return false;
                }
            }
        } else {
            CertifiedProductSearchDetails productCasted = (CertifiedProductSearchDetails) product;
            for (CertificationResult certCriteria : productCasted.getCertificationResults()) {
                if (certCriteria.getNumber().equals("170.314 (a)(1)") && certCriteria.isSuccess()) {
                    hasA1 = true;
                }
                if (certCriteria.getNumber().equals("170.314 (a)(18)") && certCriteria.isSuccess()) {
                    hasA18 = true;
                }
                if (certCriteria.getNumber().equals("170.314 (a)(19)") && certCriteria.isSuccess()) {
                    hasA19 = true;
                }
                if (certCriteria.getNumber().equals("170.314 (a)(20)") && certCriteria.isSuccess()) {
                    hasA20 = true;
                }
            } if (!hasA1) {
                if (!hasA18 || !hasA19 || !hasA20) {
                    productCasted.getErrorMessages()
                    .add("Neither (a)(1) nor the combination of (a)(18), (a)(19), and (a)(20) were found.");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if comparing cert is in certs.
     * @param certToCompare cert to look for
     * @param certs array to check
     * @return true if...wait, what does this do?
     */
    public boolean certCheck(final PendingCertificationResultDTO certToCompare, final String[] certs){
        for (String cert : certs) {
            if (!certToCompare.getNumber().equals(cert)){
                return false;
            }
        }
        return true;
    }

    /**
     * Check for G1/G2 tools.
     * @param certs certs to check
     * @param product product to check
     */
    public void g1g2TestToolCheck(final String[] certs, final PendingCertifiedProductDTO product){
        for (PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria() == Boolean.TRUE) {
                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP)
                        && cert.getGap() == Boolean.TRUE) {
                    gapEligibleAndTrue = true;
                }

                if (!gapEligibleAndTrue
                        && certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_TOOLS_USED)
                        && certCheck(cert, certs)
                        && (cert.getTestTools() == null || cert.getTestTools().size() == 0)) {
                    product.getErrorMessages()
                    .add("Test Tools are required for certification " + cert.getNumber() + ".");
                }
            }
        }
    }

    @Override
    public void validate(final PendingCertifiedProductDTO product) {
        super.validate(product);

        if (product.getPracticeTypeId() == null) {
            product.getErrorMessages().add("Practice setting is required but was not found.");
        }
        if (product.getProductClassificationId() == null) {
            product.getErrorMessages().add("Product classification is required but was not found.");
        }
        if (StringUtils.isEmpty(product.getReportFileLocation())) {
            product.getErrorMessages().add("Test Report URL is required but was not found.");
        }
        // else if(urlRegex.matcher(product.getReportFileLocation()).matches()
        // == false) {
        // product.getErrorMessages().add("Test Report URL provided is not a
        // valid URL format.");
        // }

        if (product.getHasQms() != null && product.getHasQms() && product.getQmsStandards().isEmpty()) {
            product.getWarningMessages().add(getMessage("listing.missingQMSStandards"));
        }

        // check cqms
        boolean isCqmRequired = false;
        for (PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
            if (cert.getMeetsCriteria()) {
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.G1_SUCCESS)
                        && cert.getG1Success() == null) {
                    product.getErrorMessages().add(
                            getMessage("listing.criteria.missingG1Success", cert.getNumber()));
                }
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.G2_SUCCESS)
                        && cert.getG2Success() == null) {
                    product.getErrorMessages().add(
                            getMessage("listing.criteria.missingG2Success", cert.getNumber()));
                }
                for (int i = 0; i < CQM_REQUIRED_CERTS.length; i++) {
                    if (cert.getNumber().equals(CQM_REQUIRED_CERTS[i])) {
                        isCqmRequired = true;
                    }
                }
            }
        }
        if (isCqmRequired) {
            boolean hasOneCqmWithVersion = false;
            for (PendingCqmCriterionDTO cqm : product.getCqmCriterion()) {
                if (cqm.isMeetsCriteria() && !StringUtils.isEmpty(cqm.getVersion())) {
                    hasOneCqmWithVersion = true;
                }
            }
            if (!hasOneCqmWithVersion) {
                product.getErrorMessages().add(getMessage("listing.criteria.missingCQM"));
            }
        }

        // g4, g3 and g3 complement check
        boolean hasG4 = false;
        boolean hasG3 = false;
        boolean hasG3Complement = false;
        for (PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
            if (cert.getNumber().equals("170.314 (g)(4)") && cert.getMeetsCriteria()) {
                hasG4 = true;
            }
            if (cert.getNumber().equals("170.314 (g)(3)") && cert.getMeetsCriteria()) {
                hasG3 = true;
            }
            for (int i = 0; i < G3_COMPLEMENTARY_CERTS.length; i++) {
                if (cert.getNumber().equals(G3_COMPLEMENTARY_CERTS[i]) && cert.getMeetsCriteria()) {
                    hasG3Complement = true;
                }
            }
        }
        if (!hasG4) {
            product.getErrorMessages().add(getMessage("listing.criteria.missing170314g4"));
        }
        if (hasG3 && !hasG3Complement) {
            product.getErrorMessages().add(getMessage("listing.criteria.missingG3complement"));
        }
        if (hasG3Complement && !hasG3) {
            product.getErrorMessages().add(getMessage("listing.criteria.missingComplementG3"));
        }

        // check (g)(1)
        boolean hasG1Cert = false;
        for (PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
            if (certCriteria.getNumber().equals("170.314 (g)(1)") && certCriteria.getMeetsCriteria()) {
                hasG1Cert = true;
            }
        }
        if (hasG1Cert) {
            String[] g1Certs = getG1ComplimentaryCerts();
            boolean hasG1Complement = false;
            for (int i = 0; i < g1Certs.length && !hasG1Complement; i++) {
                for (PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
                    if (certCriteria.getNumber().equals(g1Certs[i]) && certCriteria.getMeetsCriteria()) {
                        hasG1Complement = true;
                    }
                }
            }

            if (!hasG1Complement) {
                product.getWarningMessages().add(getMessage("listing.criteria.missingG1Related"));
            }
        }

        // check (g)(2)
        boolean hasG2Cert = false;
        for (PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
            if (certCriteria.getNumber().equals("170.314 (g)(2)") && certCriteria.getMeetsCriteria()) {
                hasG2Cert = true;
            }
        }
        if (hasG2Cert) {
            String[] g2Certs = getG2ComplimentaryCerts();
            boolean hasG2Complement = false;
            for (int i = 0; i < g2Certs.length && !hasG2Complement; i++) {
                for (PendingCertificationResultDTO certCriteria : product.getCertificationCriterion()) {
                    if (certCriteria.getNumber().equals(g2Certs[i]) && certCriteria.getMeetsCriteria()) {
                        hasG2Complement = true;
                    }
                }
            }

            if (!hasG2Complement) {
                product.getWarningMessages().add(getMessage("listing.criteria.missingG2Related"));
            }
        }

        if (hasG1Cert && hasG2Cert) {
            product.getWarningMessages().add(getMessage("listing.criteria.G1G2Found"));
        }
    }

    @Override
    protected void validateDemographics(final PendingCertifiedProductDTO product) {
        super.validateDemographics(product);

        for (PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria() == Boolean.TRUE) {
                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP)
                        && cert.getGap() == Boolean.TRUE) {
                    gapEligibleAndTrue = true;
                }

                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.SED)) {
                    if (cert.getSed() == null) {
                        product.getErrorMessages().add(
                                getMessage("listing.criteria.SEDRequired", cert.getNumber()));
                    } else if (cert.getSed() != null && cert.getSed().booleanValue()
                            && (cert.getUcdProcesses() == null || cert.getUcdProcesses().size() == 0)) {
                        if (product.getIcs() != null && product.getIcs().booleanValue()) {
                            product.getWarningMessages().add(
                                    getMessage("listing.criteria.missingUcdProccesses", cert.getNumber()));
                        } else {
                            product.getErrorMessages().add(
                                    getMessage("listing.criteria.missingUcdProccesses", cert.getNumber()));
                        }
                    }
                }

                if (!gapEligibleAndTrue && certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_DATA)
                        && (cert.getTestData() == null || cert.getTestData().size() == 0)) {
                    product.getErrorMessages().add("Test Data is required for certification " + cert.getNumber() + ".");
                }

                if (cert.getTestTools() != null && cert.getTestTools().size() > 0) {
                    for (PendingCertificationResultTestToolDTO testTool : cert.getTestTools()) {
                        if (StringUtils.isEmpty(testTool.getName())) {
                            product.getErrorMessages().add(
                                    "There was no test tool name found for certification " + cert.getNumber() + ".");
                        } else {
                            TestToolDTO tt = super.testToolDao.getByName(testTool.getName());
                            if (tt == null) {
                                product.getErrorMessages().add("No test tool with " + testTool.getName()
                                + " was found for criteria " + cert.getNumber() + ".");
                            } else if (tt.isRetired() && super.icsCodeInteger != null
                                    && super.icsCodeInteger.intValue() == 0) {
                                if (super.hasIcsConflict) {
                                    product.getWarningMessages().add("Test Tool '" + testTool.getName()
                                    + "' can not be used for criteria '" + cert.getNumber()
                                    + "', as it is a retired tool, and this Certified Product does not carry ICS.");
                                } else {
                                    product.getErrorMessages().add("Test Tool '" + testTool.getName()
                                    + "' can not be used for criteria '" + cert.getNumber()
                                    + "', as it is a retired tool, and this Certified Product does not carry ICS.");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final CertifiedProductSearchDetails product) {
        super.validate(product);

        if (product.getPracticeType() == null || product.getPracticeType().get("id") == null) {
            product.getErrorMessages().add("Practice setting is required but was not found.");
        }
        if (product.getClassificationType() == null || product.getClassificationType().get("id") == null) {
            product.getErrorMessages().add("Product classification is required but was not found.");
        }
        if (StringUtils.isEmpty(product.getReportFileLocation())) {
            product.getErrorMessages().add("Test Report URL is required but was not found.");
        }

        // check cqms
        boolean isCqmRequired = false;
        for (CertificationResult cert : product.getCertificationResults()) {
            for (int i = 0; i < CQM_REQUIRED_CERTS.length; i++) {
                if (cert.getNumber().equals(CQM_REQUIRED_CERTS[i]) && cert.isSuccess()) {
                    isCqmRequired = true;
                }
            }
            if (cert.isSuccess() != null && cert.isSuccess() == Boolean.TRUE) {
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.SED)) {
                    if (cert.isSed() == null) {
                        product.getErrorMessages().add(
                                getMessage("listing.criteria.SEDRequired", cert.getNumber()));
                    } else if (cert.isSed() != null && cert.isSed().booleanValue()
                            && !certHasUcdProcess(cert, product.getSed().getUcdProcesses())) {
                        if (product.getIcs() != null && product.getIcs().getInherits() != null &&
                                product.getIcs().getInherits().booleanValue()) {
                            product.getWarningMessages().add(
                                    getMessage("listing.criteria.missingUcdProccesses", cert.getNumber()));
                        } else {
                            product.getErrorMessages().add(
                                    getMessage("listing.criteria.missingUcdProccesses", cert.getNumber()));
                        }
                    }
                }
            }
        }
        if (isCqmRequired) {
            boolean hasOneCqmWithVersion = false;
            for (CQMResultDetails cqm : product.getCqmResults()) {
                if (cqm.isSuccess() && cqm.getSuccessVersions() != null && cqm.getSuccessVersions().size() > 0) {
                    hasOneCqmWithVersion = true;
                }
            }
            if (!hasOneCqmWithVersion) {
                product.getErrorMessages().add(getMessage("listing.criteria.missingCQM"));
            }
        }

        // g4 check
        boolean hasG4 = false;
        for (CertificationResult cert : product.getCertificationResults()) {
            if (cert.getNumber().equals("170.314 (g)(4)") && cert.isSuccess()) {
                hasG4 = true;
            }
        }
        if (!hasG4) {
            product.getErrorMessages().add(getMessage("lisitng.criteria.missingG4"));
        }

        // g3 check
        boolean hasG3 = false;
        for (CertificationResult cert : product.getCertificationResults()) {
            if (cert.getNumber().equals("170.314 (g)(3)") && cert.isSuccess()) {
                hasG3 = true;
            }
        }
        boolean hasG3Complement = false;
        for (CertificationResult cert : product.getCertificationResults()) {
            for (int i = 0; i < G3_COMPLEMENTARY_CERTS.length; i++) {
                if (cert.getNumber().equals(G3_COMPLEMENTARY_CERTS[i]) && cert.isSuccess()) {
                    hasG3Complement = true;
                }
            }
        }

        if (hasG3 && !hasG3Complement) {
            product.getErrorMessages().add(getMessage("listing.criteria.missingG3complement"));
        }
        if (hasG3Complement && !hasG3) {
            product.getErrorMessages().add(getMessage("listing.criteria.missingComplementG3"));
        }
    }

    @Override
    protected void validateDemographics(final CertifiedProductSearchDetails product) {
        super.validateDemographics(product);

        if (StringUtils.isEmpty(product.getReportFileLocation())) {
            product.getErrorMessages().add("A test result summary URL is required.");
        }
        Date sedTestingEnd = product.getSedTestingEndDate();
        if (!StringUtils.isEmpty(sedTestingEnd)) {
            product.setSedTestingEndDate(sedTestingEnd);
        }

        // Allow retired test tool only if CP ICS = true
        for (CertificationResult cert : product.getCertificationResults()) {
            if (cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0) {
                for (CertificationResultTestTool testTool : cert.getTestToolsUsed()) {
                    if (StringUtils.isEmpty(testTool.getTestToolName())) {
                        product.getErrorMessages()
                        .add("There was no test tool name found for certification " + cert.getNumber() + ".");
                    } else {
                        TestToolDTO tt = super.testToolDao.getByName(testTool.getTestToolName());
                        if (tt == null) {
                            product.getErrorMessages().add("No test tool with " + testTool.getTestToolName()
                            + " was found for criteria " + cert.getNumber() + ".");
                        } else if (tt.isRetired() && icsCodeInteger != null && icsCodeInteger.intValue() == 0) {
                            if (super.hasIcsConflict) {
                                product.getWarningMessages().add("Test Tool '" + testTool.getTestToolName()
                                + "' can not be used for criteria '" + cert.getNumber()
                                + "', as it is a retired tool, and this Certified Product does not carry ICS.");
                            } else {
                                product.getErrorMessages().add("Test Tool '" + testTool.getTestToolName()
                                + "' can not be used for criteria '" + cert.getNumber()
                                + "', as it is a retired tool, and this Certified Product does not carry ICS.");
                            }
                        } else if (tt.isRetired() && (product.getIcs() == null || product.getIcs().getInherits() == null
                                || product.getIcs().getInherits().equals(Boolean.FALSE))) {
                            product.getErrorMessages().add("Test Tool '" + testTool.getTestToolName()
                            + "' can not be used for criteria '" + cert.getNumber()
                            + "', as it is a retired tool, and this Certified Product does not carry ICS.");
                        }
                    }
                }
            }
        }

        // this is not supposed to match the list of things checked for pending
        // products
    }

    private boolean certHasUcdProcess(final CertificationResult cert, final List<UcdProcess> ucdProcesses) {
        boolean hasUcd = false;
        for (UcdProcess ucdProcess : ucdProcesses) {
            for (CertificationCriterion ucdCriteria : ucdProcess.getCriteria()) {
                if (ucdCriteria.getNumber().equalsIgnoreCase(cert.getNumber())) {
                    hasUcd = true;
                }
            }
        }
        return hasUcd;
    }
}
