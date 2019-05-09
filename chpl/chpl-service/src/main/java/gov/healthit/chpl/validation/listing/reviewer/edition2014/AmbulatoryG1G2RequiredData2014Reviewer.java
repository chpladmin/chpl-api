package gov.healthit.chpl.validation.listing.reviewer.edition2014;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

/**
 * Give warning messages for mismatched G1/G2 criteria for 2014 Ambulatory Listings.
 * @author alarned
 *
 */
@Component("ambulatoryG1G2RequiredData2014Reviewer")
public class AmbulatoryG1G2RequiredData2014Reviewer implements Reviewer {
    private static final String[] G1_COMPLEMENTARY_CERTS = {
            "170.314 (a)(1)", "170.314 (a)(3)", "170.314 (a)(4)", "170.314 (a)(5)", "170.314 (a)(6)", "170.314 (a)(7)",
            "170.314 (a)(9)", "170.314 (a)(11)", "170.314 (a)(12)", "170.314 (a)(13)", "170.314 (a)(14)",
            "170.314 (a)(15)", "170.314 (a)(18)", "170.314 (a)(19)", "170.314 (a)(20)", "170.314 (b)(2)",
            "170.314 (b)(3)", "170.314 (b)(4)", "170.314 (e)(1)",
            "170.314 (b)(5)(A)", "170.314 (e)(2)", "170.314 (e)(3)"
    };

    private static final String[] G2_COMPLEMENTARY_CERTS = {
            "170.314 (a)(1)", "170.314 (a)(3)", "170.314 (a)(4)", "170.314 (a)(5)", "170.314 (a)(6)", "170.314 (a)(7)",
            "170.314 (a)(9)", "170.314 (a)(11)", "170.314 (a)(12)", "170.314 (a)(13)", "170.314 (a)(14)",
            "170.314 (a)(15)", "170.314 (a)(18)", "170.314 (a)(19)", "170.314 (a)(20)", "170.314 (b)(2)",
            "170.314 (b)(3)", "170.314 (b)(4)", "170.314 (e)(1)",
            "170.314 (b)(5)(A)", "170.314 (e)(2)", "170.314 (e)(3)"
    };

    private static final String G1_2014 = "170.314 (g)(1)";
    private static final String G2_2014 = "170.314 (g)(2)";
    @Autowired private ErrorMessageUtil msgUtil;

    @Override
    public void review(final CertifiedProductSearchDetails listing) {
        boolean hasG1Cert = false;
        for (CertificationResult certCriteria : listing.getCertificationResults()) {
            if (certCriteria.getNumber().equals(G1_2014) && certCriteria.isSuccess()) {
                hasG1Cert = true;
            }
        }
        if (hasG1Cert) {
            boolean hasAtLeastOneCertPartner = false;
            for (int i = 0; i < G1_COMPLEMENTARY_CERTS.length && !hasAtLeastOneCertPartner; i++) {
                for (CertificationResult certCriteria : listing.getCertificationResults()) {
                    if (certCriteria.getNumber().equals(G1_COMPLEMENTARY_CERTS[i]) && certCriteria.isSuccess()) {
                        hasAtLeastOneCertPartner = true;
                    }
                }
            }

            if (!hasAtLeastOneCertPartner) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.missingG1Related"));
            }
        }

        // check (g)(2)
        boolean hasG2Cert = false;
        for (CertificationResult certCriteria : listing.getCertificationResults()) {
            if (certCriteria.getNumber().equals(G2_2014) && certCriteria.isSuccess()) {
                hasG2Cert = true;
            }
        }
        if (hasG2Cert) {
            boolean hasAtLeastOneCertPartner = false;
            for (int i = 0; i < G2_COMPLEMENTARY_CERTS.length && !hasAtLeastOneCertPartner; i++) {
                for (CertificationResult certCriteria : listing.getCertificationResults()) {
                    if (certCriteria.getNumber().equals(G2_COMPLEMENTARY_CERTS[i]) && certCriteria.isSuccess()) {
                        hasAtLeastOneCertPartner = true;
                    }
                }
            }

            if (!hasAtLeastOneCertPartner) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.missingG2Related"));
            }
        }

        if (hasG1Cert && hasG2Cert) {
            listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.G1G2Found"));
        }
    }
}
