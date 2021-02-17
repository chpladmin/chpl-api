package gov.healthit.chpl.surveillance.report.builder2019;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.surveillance.report.PrivilegedSurveillanceDAO;
import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.builder.ActivitiesAndOutcomesWorksheetBuilder;

@Component
public class ActivitiesAndOutcomesWorksheetBuilder2019 extends ActivitiesAndOutcomesWorksheetBuilder {

    @Autowired
    public ActivitiesAndOutcomesWorksheetBuilder2019(SurveillanceReportManager reportManager,
            CertifiedProductDetailsManager detailsManager,
            SurveillanceManager survManager, PrivilegedSurveillanceDAO privilegedSurvDao) {
        super(reportManager, detailsManager, survManager, privilegedSurvDao);
    }

    protected String getNonDisclosureEvaluationDescription() {
        return "If a suspected non-conformity resulted from the non-disclosure of material "
                + "information by the developer about limitations or additional types of costs associated "
                + "with the Complete EHR or Health IT Module, how did the ONC-ACB evaluate the suspected "
                + "non-conformity?";
    }
}
