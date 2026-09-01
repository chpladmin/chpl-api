package gov.healthit.chpl.certifiedproduct.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.certificationCriteria.CertificationCriteriaManager;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionWithAttributes;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.standard.StandardManager;
import gov.healthit.chpl.upload.listing.HeadingPostHti5;
import gov.healthit.chpl.upload.listing.HeadingPreHti5;
import gov.healthit.chpl.upload.listing.LegacyHeading;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ListingCsvHeadingWriter {

    private CertificationCriterionService criteriaService;
    private CertificationCriteriaManager criteriaManager;
    private StandardManager standardManager;
    private FF4j ff4j;

    @Autowired
    public ListingCsvHeadingWriter(CertificationCriterionService criteriaService,
            CertificationCriteriaManager criteriaManager,
            StandardManager standardManager,
            FF4j ff4j) {
        this.criteriaService = criteriaService;
        this.criteriaManager = criteriaManager;
        this.standardManager = standardManager;
        this.ff4j = ff4j;
    }

    public List<String> getCsvHeadings(CertifiedProductSearchDetails listing) {
        List<String> headings = Stream.of(
                HeadingPostHti5.UNIQUE_ID.getHeading(),
                HeadingPostHti5.DEVELOPER.getHeading(),
                HeadingPostHti5.PRODUCT.getHeading(),
                HeadingPostHti5.VERSION.getHeading())
            .collect(Collectors.toList());

        if (!ff4j.check(FeatureList.HTI_5_2027_01_01)) {
            headings.addAll(Stream.of(
                    HeadingPreHti5.MEASURE_DOMAIN.getHeading(),
                    HeadingPreHti5.MEASURE_REQUIRED_TEST.getHeading(),
                    HeadingPreHti5.MEASURE_TYPE.getHeading(),
                    HeadingPreHti5.MEASURE_ASSOCIATED_CRITERIA.getHeading())
                .collect(Collectors.toList()));
        }

        headings.addAll(Stream.of(
                HeadingPostHti5.ACB_CERTIFICATION_ID.getHeading(),
                HeadingPostHti5.CERTIFICATION_BODY_NAME.getHeading(),
                HeadingPostHti5.TESTING_LAB_NAME.getHeading(),
                HeadingPostHti5.CERTIFICATION_DATE.getHeading(),
                HeadingPostHti5.DEVELOPER_ADDRESS.getHeading(),
                HeadingPostHti5.DEVELOPER_CITY.getHeading(),
                HeadingPostHti5.DEVELOPER_STATE.getHeading(),
                HeadingPostHti5.DEVELOPER_ZIP.getHeading(),
                HeadingPostHti5.DEVELOPER_WEBSITE.getHeading(),
                HeadingPostHti5.SELF_DEVELOPER.getHeading(),
                HeadingPostHti5.DEVELOPER_EMAIL.getHeading(),
                HeadingPostHti5.DEVELOPER_PHONE.getHeading(),
                HeadingPostHti5.DEVELOPER_CONTACT_NAME.getHeading(),
                HeadingPostHti5.SVAP_NOTICE_URL.getHeading())
            .collect(Collectors.toList()));

        if (!ff4j.check(FeatureList.HTI_5_ERD)) {
            headings.addAll(Stream.of(
                    HeadingPreHti5.RWT_PLANS_URL.getHeading(),
                    HeadingPreHti5.RWT_PLANS_CHECK_DATE.getHeading())
                    .collect(Collectors.toList()));
        }

        headings.addAll(Stream.of(
                HeadingPostHti5.RWT_RESULTS_URL.getHeading(),
                HeadingPostHti5.RWT_RESULTS_CHECK_DATE.getHeading(),
                HeadingPostHti5.TARGETED_USERS.getHeading(),
                HeadingPostHti5.QMS_STANDARD_NAME.getHeading(),
                HeadingPostHti5.QMS_STANDARD_APPLICABLE_CRITERIA.getHeading(),
                HeadingPostHti5.QMS_MODIFICATION.getHeading(),
                HeadingPostHti5.ICS.getHeading(),
                HeadingPostHti5.ICS_SOURCE.getHeading(),
                HeadingPostHti5.ACCESSIBILITY_CERTIFIED.getHeading(),
                HeadingPostHti5.ACCESSIBILITY_STANDARD.getHeading(),
                HeadingPostHti5.K_1_URL.getHeading(),
                HeadingPostHti5.CQM_NUMBER.getHeading(),
                HeadingPostHti5.CQM_VERSION.getHeading(),
                HeadingPostHti5.CQM_CRITERIA.getHeading())
                .collect(Collectors.toList()));

        if (!ff4j.check(FeatureList.HTI_5_ERD)) {
            headings.addAll(Stream.of(
                HeadingPreHti5.SED_REPORT_URL.getHeading(),
                HeadingPreHti5.SED_INTENDED_USERS.getHeading(),
                HeadingPreHti5.SED_TESTING_DATE.getHeading(),
                HeadingPreHti5.PARTICIPANT_ID.getHeading(),
                HeadingPreHti5.PARTICIPANT_GENDER.getHeading(),
                HeadingPreHti5.PARTICIPANT_AGE.getHeading(),
                HeadingPreHti5.PARTICIPANT_EDUCATION.getHeading(),
                HeadingPreHti5.PARTICIPANT_OCCUPATION.getHeading(),
                HeadingPreHti5.PARTICIPANT_PROFESSIONAL_EXPERIENCE.getHeading(),
                HeadingPreHti5.PARTICIPANT_COMPUTER_EXPERIENCE.getHeading(),
                HeadingPreHti5.PARTICIPANT_PRODUCT_EXPERIENCE.getHeading(),
                HeadingPreHti5.PARTICIPANT_ASSISTIVE_TECH.getHeading(),
                HeadingPreHti5.TASK_ID.getHeading(),
                HeadingPreHti5.TASK_DESCRIPTION.getHeading(),
                HeadingPreHti5.TASK_SUCCESS_MEAN.getHeading(),
                HeadingPreHti5.TASK_SUCCESS_STDDEV.getHeading(),
                HeadingPreHti5.TASK_PATH_DEV_OBS.getHeading(),
                HeadingPreHti5.TASK_PATH_DEV_OPT.getHeading(),
                HeadingPreHti5.TASK_TIME_MEAN.getHeading(),
                HeadingPreHti5.TASK_TIME_STDDEV.getHeading(),
                HeadingPreHti5.TASK_TIME_DEV_OBS.getHeading(),
                HeadingPreHti5.TASK_TIME_DEV_OPT.getHeading(),
                HeadingPreHti5.TASK_ERRORS_MEAN.getHeading(),
                HeadingPreHti5.TASK_ERRORS_STDDEV.getHeading(),
                HeadingPreHti5.TASK_RATING_SCALE.getHeading(),
                HeadingPreHti5.TASK_RATING.getHeading(),
                HeadingPreHti5.TASK_RATING_STDDEV.getHeading())
                    .collect(Collectors.toList()));
        }

        headings.addAll(getCriteriaHeadings(listing));
        return headings;
    }

    private List<String> getCriteriaHeadings(CertifiedProductSearchDetails listing) {
        List<String> criteriaHeadings = new ArrayList<String>();
        List<CertificationCriterion> allCriteriaAvailableToListing = criteriaManager.getCriteriaAvailableToListingAndUser(listing);
        allCriteriaAvailableToListing.stream()
            .forEach(certResult -> criteriaHeadings.addAll(getCriterionHeadings(certResult)));
        return criteriaHeadings;
    }

    private List<String> getCriterionHeadings(CertificationCriterion criterion) {
        List<String> criterionHeadings = new ArrayList<String>();

        criterionHeadings.add(getCriterionNumberHeading(criterion));
        CertificationCriterionWithAttributes criterionWithAttributes = criteriaManager.getAllWithAttributes().stream()
                .filter(critWithAttr -> critWithAttr.getId().equals(criterion.getId()))
                .findAny().orElse(null);
        if (criterionWithAttributes == null) {
            LOGGER.error("Attributes for criterion with ID " + criterion.getId() + " could not be found. No attributes will be included in the file.");
        } else {
            if (criterionWithAttributes.getAttributes().isAdditionalSoftware()) {
                criterionHeadings.add(HeadingPostHti5.HAS_ADDITIONAL_SOFTWARE.getHeading());
                criterionHeadings.add(HeadingPostHti5.ADDITIONAL_SOFTWARE_LISTING.getHeading());
                criterionHeadings.add(HeadingPostHti5.ADDITIONAL_SOFTWARE_LISTING_GROUPING.getHeading());
                criterionHeadings.add(HeadingPostHti5.ADDITIONAL_SOFTWARE_NONLISTING.getHeading());
                criterionHeadings.add(HeadingPostHti5.ADDITIONAL_SOFTWARE_NONLISTING_VERSION.getHeading());
                criterionHeadings.add(HeadingPostHti5.ADDITIONAL_SOFTWARE_NONLISTING_GROUPING.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isApiDocumentation()) {
                criterionHeadings.add(HeadingPostHti5.API_DOCUMENTATION_LINK.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isAttestationAnswer()) {
                criterionHeadings.add(HeadingPostHti5.ATTESTATION_ANSWER.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isCodeSet()) {
                criterionHeadings.add(HeadingPostHti5.CODE_SET.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isConformanceMethod()) {
                criterionHeadings.add(HeadingPostHti5.CONFORMANCE_METHOD.getHeading());
                criterionHeadings.add(HeadingPostHti5.CONFORMANCE_METHOD_VERSION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isDocumentationUrl()) {
                criterionHeadings.add(HeadingPostHti5.DOCUMENTATION_URL.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isExportDocumentation()) {
                criterionHeadings.add(HeadingPostHti5.EXPORT_DOCUMENTATION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isFunctionalityTested()) {
                criterionHeadings.add(HeadingPostHti5.FUNCTIONALITIES_TESTED.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isG1Success()) {
                criterionHeadings.add(LegacyHeading.MACRA_MEASURE_G1.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isG2Success()) {
                criterionHeadings.add(LegacyHeading.MACRA_MEASURE_G2.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isOptionalStandard()) {
                criterionHeadings.add(HeadingPostHti5.OPTIONAL_STANDARD.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isPrivacySecurityFramework()) {
                criterionHeadings.add(HeadingPostHti5.PRIVACY_AND_SECURITY.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isRiskManagementSummaryInformation()) {
                criterionHeadings.add(HeadingPostHti5.RISK_MANAGEMENT_SUMMARY_INFORMATION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isSed()) {
                criterionHeadings.add(HeadingPostHti5.UCD_PROCESS.getHeading());
                criterionHeadings.add(HeadingPostHti5.UCD_PROCESS_DETAILS.getHeading());
                if (!ff4j.check(FeatureList.HTI_5_ERD)) {
                    criterionHeadings.add(HeadingPreHti5.TASK_ID.getHeading());
                    criterionHeadings.add(HeadingPreHti5.PARTICIPANT_ID.getHeading());
                }
            }
            if (criterionWithAttributes.getAttributes().isServiceBaseUrlList()) {
                criterionHeadings.add(HeadingPostHti5.SERVICE_BASE_URL_LIST.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isStandard()
                    //we seem to list all criteria as eligible for standards, so here
                    //we are also limiting the column presence by checking whether the criteria
                    //has any standards available
                    && !CollectionUtils.isEmpty(standardManager.getStandardsByCriteria(criterion.getId()))) {
                criterionHeadings.add(HeadingPostHti5.STANDARD.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isStandardsTested()) {
                criterionHeadings.add(LegacyHeading.TEST_STANDARD.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isSvap()) {
                criterionHeadings.add(HeadingPostHti5.SVAP_REG_TEXT.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isTestProcedure()) {
                criterionHeadings.add(LegacyHeading.TEST_PROCEDURE.getHeading());
                criterionHeadings.add(LegacyHeading.TEST_PROCEDURE_VERSION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isTestTool()) {
                criterionHeadings.add(HeadingPostHti5.TEST_TOOL_NAME.getHeading());
                criterionHeadings.add(HeadingPostHti5.TEST_TOOL_VERSION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isUseCases()) {
                criterionHeadings.add(HeadingPostHti5.USE_CASES.getHeading());
            }
        }
        return criterionHeadings;
    }

    private String getCriterionNumberHeading(CertificationCriterion criterion) {
        List<String> criterionHeadings = criteriaService.getCriterionHeadings(criterion.getId());
        if (CollectionUtils.isEmpty(criterionHeadings)) {
            LOGGER.error("No criterion heading was added for criterion with ID " + criterion.getId());
            return "";
        }
        return criterionHeadings.get(0);
    }
}
