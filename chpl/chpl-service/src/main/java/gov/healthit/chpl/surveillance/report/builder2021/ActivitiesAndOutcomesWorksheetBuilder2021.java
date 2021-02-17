package gov.healthit.chpl.surveillance.report.builder2021;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.surveillance.report.PrivilegedSurveillanceDAO;
import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.builder.ActivitiesAndOutcomesWorksheetBuilder;

@Component
public class ActivitiesAndOutcomesWorksheetBuilder2021 extends ActivitiesAndOutcomesWorksheetBuilder {

    @Autowired
    public ActivitiesAndOutcomesWorksheetBuilder2021(SurveillanceReportManager reportManager,
            CertifiedProductDetailsManager detailsManager,
            SurveillanceManager survManager, PrivilegedSurveillanceDAO privilegedSurvDao) {
        super(reportManager, detailsManager, survManager, privilegedSurvDao);
    }

    protected String getNonDisclosureEvaluationDescription() {
        return "If a suspected non-conformity resulted from the non-disclosure of material "
                + "information by the developer about additional types of costs associated with "
                + "the Health IT Module, how did the ONC-ACB evaluate the suspected non-conformity?";
    }
}
