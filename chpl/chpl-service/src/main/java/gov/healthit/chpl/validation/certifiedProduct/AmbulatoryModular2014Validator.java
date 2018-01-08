package gov.healthit.chpl.validation.certifiedProduct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.util.CertificationResultRules;

@Component("ambulatoryModular2014Validator")
public class AmbulatoryModular2014Validator extends CertifiedProduct2014Validator {
    private static final String[] g1ComplementaryCerts = {
            "170.314 (b)(5)(A)", "170.314 (e)(2)", "170.314 (e)(3)"
    };
    private static final String[] g2ComplementaryCerts = {
            "170.314 (b)(5)(A)", "170.314 (e)(2)", "170.314 (e)(3)"
    };
    private static final String[] g1g2TestToolCheckCerts = {
    	"170.314 (g)(1)", "170.314 (g)(2)", "170.314 (f)(3)"
    };

    @Autowired
    CertifiedProductDetailsManager cpdManager;

    @Override
    public String[] getG1ComplimentaryCerts() {
        String[] certs = super.getG1ComplimentaryCerts();
        String[] allCerts = new String[certs.length + g1ComplementaryCerts.length];

        int allCertIndex = 0;

        for (int j = 0; j < certs.length; j++) {
            allCerts[allCertIndex] = new String(certs[j]);
            allCertIndex++;
        }
        for (int j = 0; j < g1ComplementaryCerts.length; j++) {
            allCerts[allCertIndex] = new String(g1ComplementaryCerts[j]);
            allCertIndex++;
        }
        return allCerts;
    }

    @Override
    public String[] getG2ComplimentaryCerts() {
        String[] certs = super.getG2ComplimentaryCerts();
        String[] allCerts = new String[certs.length + g2ComplementaryCerts.length];

        int allCertIndex = 0;
        for (int j = 0; j < certs.length; j++) {
            allCerts[allCertIndex] = new String(certs[j]);
            allCertIndex++;
        }

        for (int j = 0; j < g2ComplementaryCerts.length; j++) {
            allCerts[allCertIndex] = new String(g2ComplementaryCerts[j]);
            allCertIndex++;
        }

        return allCerts;
    }

    public String getErrorMessage(String errorField){
    	return String.format(
    			messageSource.getMessage(new DefaultMessageSourceResolvable(errorField),
    					LocaleContextHolder.getLocale()));
    }

    @Override
    protected void validateDemographics(PendingCertifiedProductDTO product) {
        super.validateDemographics(product);
        super.g1g2TestToolCheck(g1g2TestToolCheckCerts, product);
    }

    @Override
    public void validate(PendingCertifiedProductDTO product) {
        super.validate(product);

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
                product.getErrorMessages().add(getErrorMessage("listing.criteria.missingG1Related"));
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
                product.getErrorMessages().add(getErrorMessage("listing.criteria.missingG2Related"));
            }
        }

        if (hasG1Cert && hasG2Cert) {
            product.getWarningMessages().add(getErrorMessage("listing.criteria.G1G2Found"));
        }
    }

    @Override
    protected void validateDemographics(CertifiedProductSearchDetails product) {
        super.validateDemographics(product);

        // NOTE: this is not supposed to match the list of things checked for
        // pending products
    }

    @Override
    public void validate(CertifiedProductSearchDetails product) {
        super.validate(product);

        // check (g)(1)
        boolean hasG1Cert = false;
        for (CertificationResult certCriteria : product.getCertificationResults()) {
            if (certCriteria.getNumber().equals("170.314 (g)(1)") && certCriteria.isSuccess()) {
                hasG1Cert = true;
            }
        }
        if (hasG1Cert) {
            String[] g1Certs = getG1ComplimentaryCerts();
            boolean hasAtLeastOneCertPartner = false;
            for (int i = 0; i < g1Certs.length && !hasAtLeastOneCertPartner; i++) {
                for (CertificationResult certCriteria : product.getCertificationResults()) {
                    if (certCriteria.getNumber().equals(g1Certs[i]) && certCriteria.isSuccess()) {
                        hasAtLeastOneCertPartner = true;
                    }
                }
            }

            if (!hasAtLeastOneCertPartner) {
                product.getWarningMessages().add(getErrorMessage("listing.criteria.missingG1Related"));
            }
        }

        // check (g)(2)
        boolean hasG2Cert = false;
        for (CertificationResult certCriteria : product.getCertificationResults()) {
            if (certCriteria.getNumber().equals("170.314 (g)(2)") && certCriteria.isSuccess()) {
                hasG2Cert = true;
            }
        }
        if (hasG2Cert) {
            String[] g2Certs = getG2ComplimentaryCerts();
            boolean hasAtLeastOneCertPartner = false;
            for (int i = 0; i < g2Certs.length && !hasAtLeastOneCertPartner; i++) {
                for (CertificationResult certCriteria : product.getCertificationResults()) {
                    if (certCriteria.getNumber().equals(g2Certs[i]) && certCriteria.isSuccess()) {
                        hasAtLeastOneCertPartner = true;
                    }
                }
            }

            if (!hasAtLeastOneCertPartner) {
                product.getWarningMessages().add(getErrorMessage("listing.criteria.missingG2Related"));
            }
        }

        if (hasG1Cert && hasG2Cert) {
            product.getWarningMessages().add(getErrorMessage("listing.criteria.G1G2Found"));
        }
    }
}
