package gov.healthit.chpl.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.SurveillanceOversightRule;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

public abstract class SurveillanceOversightReportApp extends NotificationEmailerReportApp {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceOversightReportApp.class);

    protected static final String TRIGGER_DESCRIPTIONS = "<h4>Description of Surveillance Rules</h4>" + "<ol>" + "<li>"
            + SurveillanceOversightRule.LONG_SUSPENSION.getTitle() + ": "
            + SurveillanceOversightRule.LONG_SUSPENSION.getDescription() + "</li>" + "<li>"
            + SurveillanceOversightRule.CAP_NOT_APPROVED.getTitle() + ": "
            + SurveillanceOversightRule.CAP_NOT_APPROVED.getDescription() + "</li>" + "<li>"
            + SurveillanceOversightRule.CAP_NOT_STARTED.getTitle() + ": "
            + SurveillanceOversightRule.CAP_NOT_STARTED.getDescription() + "</li>" + "<li>"
            + SurveillanceOversightRule.CAP_NOT_COMPLETED.getTitle() + ": "
            + SurveillanceOversightRule.CAP_NOT_COMPLETED.getDescription() + "</li>" + "<li>"
            + SurveillanceOversightRule.CAP_NOT_CLOSED.getTitle() + ": "
            + SurveillanceOversightRule.CAP_NOT_CLOSED.getDescription() + "</li>" + "<li>"
            + SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE.getTitle() + ": "
            + SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE.getDescription() + "</li>" + "</ol>";

    public SurveillanceOversightReportApp() {
        super();
    }

    @Override
    public List<CertifiedProductSearchDetails> getAllCertifiedProductSearchDetails() {
        List<CertifiedProductDetailsDTO> allCertifiedProducts = this.getCertifiedProductDAO().findWithSurveillance();
        List<CertifiedProductSearchDetails> allCertifiedProductDetails = new ArrayList<CertifiedProductSearchDetails>(
                allCertifiedProducts.size());
        for (CertifiedProductDetailsDTO currProduct : allCertifiedProducts) {
            try {
                CertifiedProductSearchDetails product = this.getCpdManager()
                        .getCertifiedProductDetails(currProduct.getId());
                allCertifiedProductDetails.add(product);
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not find certified product details for certified product with id = "
                        + currProduct.getId());
            }
        }
        return allCertifiedProductDetails;
    }

    protected String createHtmlEmailBody(Map<SurveillanceOversightRule, Integer> brokenRules, String noContentMsg)
            throws IOException {
        // were any rules broken?
        boolean anyRulesBroken = hasBrokenRules(brokenRules);
        String htmlMessage = "";
        if (!anyRulesBroken) {
            htmlMessage = noContentMsg;
        } else {
            htmlMessage += "<ul>";
            htmlMessage += "<li>" + SurveillanceOversightRule.LONG_SUSPENSION.getTitle() + ": "
                    + brokenRules.get(SurveillanceOversightRule.LONG_SUSPENSION) + "</li>";
            htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_APPROVED.getTitle() + ": "
                    + brokenRules.get(SurveillanceOversightRule.CAP_NOT_APPROVED) + "</li>";
            htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_STARTED.getTitle() + ": "
                    + brokenRules.get(SurveillanceOversightRule.CAP_NOT_STARTED) + "</li>";
            htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_COMPLETED.getTitle() + ": "
                    + brokenRules.get(SurveillanceOversightRule.CAP_NOT_COMPLETED) + "</li>";
            htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_CLOSED.getTitle() + ": "
                    + brokenRules.get(SurveillanceOversightRule.CAP_NOT_CLOSED) + "</li>";
            htmlMessage += "<li>" + SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE.getTitle() + ": "
                    + brokenRules.get(SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE) + "</li>";
            htmlMessage += "</ul>";
        }

        htmlMessage += TRIGGER_DESCRIPTIONS;
        return htmlMessage;
    }

    protected Boolean hasBrokenRules(Map<SurveillanceOversightRule, Integer> brokenRules) {
        Boolean anyRulesBroken = false;
        for (SurveillanceOversightRule rule : brokenRules.keySet()) {
            Integer brokenRuleCount = brokenRules.get(rule);
            if (brokenRuleCount.intValue() > 0) {
                anyRulesBroken = true;
            }
        }
        return anyRulesBroken;
    }
}
