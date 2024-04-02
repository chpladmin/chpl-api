package gov.healthit.chpl.surveillance.report.builder2019;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.complaint.ComplaintDAO;
import gov.healthit.chpl.surveillance.report.PrivilegedSurveillanceDAO;
import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.builder.ActivitiesAndOutcomesWorksheetBuilder;

@Component
public class ActivitiesAndOutcomesWorksheetBuilder2019 extends ActivitiesAndOutcomesWorksheetBuilder {

    @Autowired
    public ActivitiesAndOutcomesWorksheetBuilder2019(SurveillanceReportManager reportManager,
            CertifiedProductDetailsManager detailsManager,
            PrivilegedSurveillanceDAO privilegedSurvDao,
            ComplaintDAO complaintDao) {
        super(reportManager, detailsManager, privilegedSurvDao, complaintDao);
    }

    @Override
    protected String getGroundsForInitiatingSurveillanceDescription() {
        return "On what grounds did the ONC-ACB initiate surveillance "
                + "(i.e., the particular facts and circumstances from which a reasonable person would "
                + "have had grounds to question the continued conformity of the Complete EHR or "
                + "Health IT Module)? For randomized surveillance, it is acceptable to state it was chosen randomly.";
    }

    @Override
    protected String getStepsToSurveilDescription() {
        return "What steps did the ONC-ACB take to surveil the Complete EHR or Health "
                + "IT Module, to analyze evidence, and to substantiate the non-conformity or non-conformities?";
    }

    @Override
    protected String getAdditionalCostsEvaluationDescription() {
        return "If a suspected non-conformity resulted from additional types of costs "
                + "that a user was required to pay in order to implement or use the Complete EHR "
                + "or Health IT Module's certified capabilities, how did ONC-ACB evaluate that "
                + "suspected non-conformity?";
    }

    @Override
    protected String getLimitationsEvaluationDescription() {
        return "If a suspected non-conformity resulted from limitations that a user "
                + "encountered in the course of implementing and using the Complete EHR or "
                + "Health IT Module's certified capabilities, how did ONC-ACB evaluate that "
                + "suspected non-conformity?";
    }

    @Override
    protected String getNonDisclosureEvaluationDescription() {
        return "If a suspected non-conformity resulted from the non-disclosure of material "
                + "information by the developer about limitations or additional types of costs associated "
                + "with the Complete EHR or Health IT Module, how did the ONC-ACB evaluate the suspected "
                + "non-conformity?";
    }
}
