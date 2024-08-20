package gov.healthit.chpl.certifiedproduct.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriteriaManager;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionWithAttributes;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.LegacyHeadings;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ListingCsvHeadingWriter {

    private CertificationCriterionService criteriaService;
    private CertificationCriteriaManager criteriaManager;

    @Autowired
    public ListingCsvHeadingWriter(CertificationCriterionService criteriaService,
            CertificationCriteriaManager criteriaManager) {
        this.criteriaService = criteriaService;
        this.criteriaManager = criteriaManager;
    }

    public List<String> getCsvHeadings(CertifiedProductSearchDetails listing) {
        List<String> headings = Stream.of(
                Headings.UNIQUE_ID.getHeading(),
                Headings.DEVELOPER.getHeading(),
                Headings.PRODUCT.getHeading(),
                Headings.VERSION.getHeading(),
                Headings.MEASURE_DOMAIN.getHeading(),
                Headings.MEASURE_REQUIRED_TEST.getHeading(),
                Headings.MEASURE_TYPE.getHeading(),
                Headings.MEASURE_ASSOCIATED_CRITERIA.getHeading(),
                Headings.ACB_CERTIFICATION_ID.getHeading(),
                Headings.CERTIFICATION_BODY_NAME.getHeading(),
                Headings.TESTING_LAB_NAME.getHeading(),
                Headings.CERTIFICATION_DATE.getHeading(),
                Headings.DEVELOPER_ADDRESS.getHeading(),
                Headings.DEVELOPER_CITY.getHeading(),
                Headings.DEVELOPER_STATE.getHeading(),
                Headings.DEVELOPER_ZIP.getHeading(),
                Headings.DEVELOPER_WEBSITE.getHeading(),
                Headings.SELF_DEVELOPER.getHeading(),
                Headings.DEVELOPER_EMAIL.getHeading(),
                Headings.DEVELOPER_PHONE.getHeading(),
                Headings.DEVELOPER_CONTACT_NAME.getHeading(),
                Headings.SVAP_NOTICE_URL.getHeading(),
                Headings.RWT_PLANS_URL.getHeading(),
                Headings.RWT_PLANS_CHECK_DATE.getHeading(),
                Headings.RWT_RESULTS_URL.getHeading(),
                Headings.RWT_RESULTS_CHECK_DATE.getHeading(),
                Headings.TARGETED_USERS.getHeading(),
                Headings.QMS_STANDARD_NAME.getHeading(),
                Headings.QMS_STANDARD_APPLICABLE_CRITERIA.getHeading(),
                Headings.QMS_MODIFICATION.getHeading(),
                Headings.ICS.getHeading(),
                Headings.ICS_SOURCE.getHeading(),
                Headings.ACCESSIBILITY_CERTIFIED.getHeading(),
                Headings.ACCESSIBILITY_STANDARD.getHeading(),
                Headings.K_1_URL.getHeading(),
                Headings.CQM_NUMBER.getHeading(),
                Headings.CQM_VERSION.getHeading(),
                Headings.CQM_CRITERIA.getHeading(),
                Headings.SED_REPORT_URL.getHeading(),
                Headings.SED_INTENDED_USERS.getHeading(),
                Headings.SED_TESTING_DATE.getHeading(),
                Headings.PARTICIPANT_ID.getHeading(),
                Headings.PARTICIPANT_GENDER.getHeading(),
                Headings.PARTICIPANT_AGE.getHeading(),
                Headings.PARTICIPANT_EDUCATION.getHeading(),
                Headings.PARTICIPANT_OCCUPATION.getHeading(),
                Headings.PARTICIPANT_PROFESSIONAL_EXPERIENCE.getHeading(),
                Headings.PARTICIPANT_COMPUTER_EXPERIENCE.getHeading(),
                Headings.PARTICIPANT_PRODUCT_EXPERIENCE.getHeading(),
                Headings.PARTICIPANT_ASSISTIVE_TECH.getHeading(),
                Headings.TASK_ID.getHeading(),
                Headings.TASK_DESCRIPTION.getHeading(),
                Headings.TASK_SUCCESS_MEAN.getHeading(),
                Headings.TASK_SUCCESS_STDDEV.getHeading(),
                Headings.TASK_PATH_DEV_OBS.getHeading(),
                Headings.TASK_PATH_DEV_OPT.getHeading(),
                Headings.TASK_TIME_MEAN.getHeading(),
                Headings.TASK_TIME_STDDEV.getHeading(),
                Headings.TASK_TIME_DEV_OBS.getHeading(),
                Headings.TASK_TIME_DEV_OPT.getHeading(),
                Headings.TASK_ERRORS_MEAN.getHeading(),
                Headings.TASK_ERRORS_STDDEV.getHeading(),
                Headings.TASK_RATING_SCALE.getHeading(),
                Headings.TASK_RATING.getHeading(),
                Headings.TASK_RATING_STDDEV.getHeading()
                ).collect(Collectors.toList());

        headings.addAll(getCriteriaHeadings(listing));
        return headings;
    }

    private List<String> getCriteriaHeadings(CertifiedProductSearchDetails listing) {
        List<String> criteriaHeadings = new ArrayList<String>();
        listing.getCertificationResults().stream()
            .forEach(certResult -> criteriaHeadings.addAll(getCriterionHeadings(certResult)));
        return criteriaHeadings;
    }

    private List<String> getCriterionHeadings(CertificationResult certResult) {
        List<String> criterionHeadings = new ArrayList<String>();

        CertificationCriterion criterion = certResult.getCriterion();
        criterionHeadings.add(getCriterionNumberHeading(criterion));
        CertificationCriterionWithAttributes criterionWithAttributes = criteriaManager.getAllWithAttributes().stream()
                .filter(critWithAttr -> critWithAttr.getId().equals(criterion.getId()))
                .findAny().orElse(null);
        if (criterionWithAttributes == null) {
            LOGGER.error("Attributes for criterion with ID " + criterion.getId() + " could not be found. No attributes will be included in the file.");
        } else {
            //TODO add all attributes
            if (criterionWithAttributes.getAttributes().isAdditionalSoftware()) {
                criterionHeadings.add(Headings.HAS_ADDITIONAL_SOFTWARE.getHeading());
                criterionHeadings.add(Headings.ADDITIONAL_SOFTWARE_LISTING.getHeading());
                criterionHeadings.add(Headings.ADDITIONAL_SOFTWARE_LISTING_GROUPING.getHeading());
                criterionHeadings.add(Headings.ADDITIONAL_SOFTWARE_NONLISTING_GROUPING.getHeading());
                criterionHeadings.add(Headings.ADDITIONAL_SOFTWARE_NONLISTING_VERSION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isApiDocumentation()) {
                criterionHeadings.add(Headings.API_DOCUMENTATION_LINK.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isAttestationAnswer()) {
                criterionHeadings.add(Headings.ATTESTATION_ANSWER.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isCodeSet()) {
                criterionHeadings.add(Headings.CODE_SET.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isConformanceMethod()) {
                criterionHeadings.add(Headings.CONFORMANCE_METHOD.getHeading());
                criterionHeadings.add(Headings.CONFORMANCE_METHOD_VERSION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isDocumentationUrl()) {
                criterionHeadings.add(Headings.DOCUMENTATION_URL.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isExportDocumentation()) {
                criterionHeadings.add(Headings.EXPORT_DOCUMENTATION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isFunctionalityTested()) {
                criterionHeadings.add(Headings.FUNCTIONALITIES_TESTED.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isG1Success()) {
                criterionHeadings.add(LegacyHeadings.MACRA_MEASURE_G1.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isG2Success()) {
                criterionHeadings.add(LegacyHeadings.MACRA_MEASURE_G2.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isGap()) {
                criterionHeadings.add(Headings.GAP.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isOptionalStandard()) {
                criterionHeadings.add(Headings.OPTIONAL_STANDARD.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isPrivacySecurityFramework()) {
                criterionHeadings.add(Headings.PRIVACY_AND_SECURITY.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isRiskManagementSummaryInformation()) {
                criterionHeadings.add(Headings.RISK_MANAGEMENT_SUMMARY_INFORMATION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isSed()) {
                criterionHeadings.add(Headings.UCD_PROCESS.getHeading());
                criterionHeadings.add(Headings.UCD_PROCESS_DETAILS.getHeading());
                criterionHeadings.add(Headings.UCD_PROCESS.getHeading());
                criterionHeadings.add(Headings.TASK_ID.getHeading());
                criterionHeadings.add(Headings.PARTICIPANT_ID.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isServiceBaseUrlList()) {
                criterionHeadings.add(Headings.SERVICE_BASE_URL_LIST.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isStandard()) {
                criterionHeadings.add(Headings.STANDARD.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isStandardsTested()) {
                criterionHeadings.add(LegacyHeadings.TEST_STANDARD.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isSvap()) {
                criterionHeadings.add(Headings.SVAP_REG_TEXT.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isTestData()) {
                criterionHeadings.add(Headings.TEST_DATA.getHeading());
                criterionHeadings.add(Headings.TEST_DATA_VERSION.getHeading());
                criterionHeadings.add(Headings.TEST_DATA_ALTERATION.getHeading());
                criterionHeadings.add(Headings.TEST_DATA_ALTERATION_DESC.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isTestProcedure()) {
                criterionHeadings.add(LegacyHeadings.TEST_PROCEDURE.getHeading());
                criterionHeadings.add(LegacyHeadings.TEST_PROCEDURE_VERSION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isTestTool()) {
                criterionHeadings.add(Headings.TEST_TOOL_NAME.getHeading());
                criterionHeadings.add(Headings.TEST_TOOL_VERSION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isUseCases()) {
                criterionHeadings.add(Headings.USE_CASES.getHeading());
            }
        }
        return criterionHeadings;
    }

    private String getCriterionNumberHeading(CertificationCriterion criterion) {
        //TODO: How would we feel about adding the column heading to each criterion in the database??
        //Not returning it in the JSON ever, but either in the certification_criterion table or maybe
        //certification_criterion_attribute?
        String criterionHeading = null;
        if (criterion.getId().equals(criteriaService.get(Criteria2015.A_1).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_A_1.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.A_2).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_A_2.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.A_3).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_A_3.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.A_4).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_A_4.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.A_5).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_A_5.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.A_6).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_A_6.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.A_7).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_A_7.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.A_8).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_A_8.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.A_9).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_A_9.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.A_10).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_A_10.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.A_11).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_A_11.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.A_12).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_A_12.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.A_13).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_A_13.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.A_14).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_A_14.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.A_15).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_A_15.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_1_OLD).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_1.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_1_CURES).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_1_CURES.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_2_OLD).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_2.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_2_CURES).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_2_CURES.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_3_OLD).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_3.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_3_CURES).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_3_CURES.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_4).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_4.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_5).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_5.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_6).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_6.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_7_OLD).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_7.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_7_CURES).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_7_CURES.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_8_OLD).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_8.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_8_CURES).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_8_CURES.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_9_OLD).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_9.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_9_CURES).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_9_CURES.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_10).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_10.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.B_11).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_B_11.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.C_1).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_C_1.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.C_2).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_C_2.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.C_3_OLD).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_C_3.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.C_3_CURES).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_C_3_CURES.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.C_4).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_C_4.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_1).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_1.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_2_OLD).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_2.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_2_CURES).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_2_CURES.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_3_OLD).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_3.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_3_CURES).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_3_CURES.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_4).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_4.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_5).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_5.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_6).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_6.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_7).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_7.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_8).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_8.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_9).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_9.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_10_OLD).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_10.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_10_CURES).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_10_CURES.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_11).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_11.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_12).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_12.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.D_13).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_D_13.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.E_1_OLD).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_E_1.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.E_1_CURES).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_E_1_CURES.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.E_2).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_E_2.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.E_3).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_E_3.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.F_1).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_F_1.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.F_2).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_F_2.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.F_3).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_F_3.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.F_4).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_F_4.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.F_5_OLD).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_F_5.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.F_5_CURES).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_F_5_CURES.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.F_6).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_F_6.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.F_7).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_F_7.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.G_1).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_G_1.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.G_2).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_G_2.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.G_3).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_G_3.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.G_4).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_G_4.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.G_5).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_G_5.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.G_6_OLD).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_G_6.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.G_6_CURES).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_G_6_CURES.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.G_7).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_G_7.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.G_8).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_G_8.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.G_9_OLD).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_G_9.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.G_9_CURES).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_G_9_CURES.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.G_10).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_G_10.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.H_1).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_H_1.getHeading();
        } else if (criterion.getId().equals(criteriaService.get(Criteria2015.H_2).getId())) {
            criterionHeading = Headings.CRITERIA_170_315_H_2.getHeading();
        } else {
            LOGGER.error("No criterion heading was added for criterion with ID " + criterion.getId());
        }
        return criterionHeading;
    }
}
