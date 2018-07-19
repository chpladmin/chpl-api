package gov.healthit.chpl.validation.certifiedProduct;

import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

@Component("inpatientComplete2014Validator")
public class InpatientComplete2014Validator extends InpatientModular2014Validator {

    private static final String[] requiredCriteria = {
            "170.314 (a)(2)", "170.314 (a)(3)", "170.314 (a)(4)", "170.314 (a)(5)", "170.314 (a)(6)", "170.314 (a)(7)",
            "170.314 (a)(8)", "170.314 (a)(9)", "170.314 (a)(10)", "170.314 (a)(11)", "170.314 (a)(12)",
            "170.314 (a)(13)", "170.314 (a)(14)", "170.314 (a)(15)", "170.314 (a)(16)", "170.314 (a)(17)",
            "170.314 (b)(3)", "170.314 (b)(4)", "170.314 (b)(5)(B)", "170.314 (b)(6)", "170.314 (b)(7)",
            "170.314 (c)(1)", "170.314 (c)(2)", "170.314 (c)(3)", "170.314 (d)(1)", "170.314 (d)(2)", "170.314 (d)(3)",
            "170.314 (d)(4)", "170.314 (d)(5)", "170.314 (d)(6)", "170.314 (d)(7)", "170.314 (d)(8)", "170.314 (e)(1)",
            "170.314 (f)(1)", "170.314 (f)(2)", "170.314 (f)(3)", "170.314 (g)(2)", "170.314 (g)(3)", "170.314 (g)(4)"
    };

    @Override
    public void validate(PendingCertifiedProductDTO product) {
        super.validate(product);
        super.checkA1OrA18A19A20(product);
        super.checkB1B2B8H1(product);

        List<PendingCertificationResultDTO> certificationCriterion = product.getCertificationCriterion();
        for (int i = 0; i < requiredCriteria.length; i++) {
            boolean hasCert = false;
            for (PendingCertificationResultDTO certCriteria : certificationCriterion) {
                if (certCriteria.getNumber().equals(requiredCriteria[i]) && certCriteria.getMeetsCriteria()) {
                    hasCert = true;
                }
            }
            if (!hasCert) {
                product.getErrorMessages()
                        .add("Required certification criteria " + requiredCriteria[i] + " was not found.");
            }
        }
    }

    @Override
    public void validate(CertifiedProductSearchDetails product) {
        super.validate(product);
//        super.checkA1OrA18A19A20(product);
//        super.checkB1B2B8H1(product);
//
//        List<CertificationResult> certificationCriterion = product.getCertificationResults();
//        for (int i = 0; i < requiredCriteria.length; i++) {
//            boolean hasCert = false;
//            for (CertificationResult certCriteria : certificationCriterion) {
//                if (certCriteria.getNumber().equals(requiredCriteria[i]) && certCriteria.isSuccess()) {
//                    hasCert = true;
//                }
//            }
//            if (!hasCert) {
//                product.getErrorMessages()
//                        .add("Required certification criteria " + requiredCriteria[i] + " was not found.");
//            }
//        }
    }
}
