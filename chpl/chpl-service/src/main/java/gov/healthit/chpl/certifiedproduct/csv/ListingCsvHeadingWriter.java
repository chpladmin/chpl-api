package gov.healthit.chpl.certifiedproduct.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriteriaManager;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionWithAttributes;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.standard.StandardManager;
import gov.healthit.chpl.upload.listing.ListingUploadHeadingUtil.Heading;
import gov.healthit.chpl.upload.listing.ListingUploadHeadingUtil.LegacyHeading;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ListingCsvHeadingWriter {

    private CertificationCriterionService criteriaService;
    private CertificationCriteriaManager criteriaManager;
    private StandardManager standardManager;

    @Autowired
    public ListingCsvHeadingWriter(CertificationCriterionService criteriaService,
            CertificationCriteriaManager criteriaManager,
            StandardManager standardManager) {
        this.criteriaService = criteriaService;
        this.criteriaManager = criteriaManager;
        this.standardManager = standardManager;
    }

    public List<String> getCsvHeadings(CertifiedProductSearchDetails listing) {
        List<String> headings = Stream.of(
                Heading.UNIQUE_ID.getHeading(),
                Heading.DEVELOPER.getHeading(),
                Heading.PRODUCT.getHeading(),
                Heading.VERSION.getHeading(),
                Heading.MEASURE_DOMAIN.getHeading(),
                Heading.MEASURE_REQUIRED_TEST.getHeading(),
                Heading.MEASURE_TYPE.getHeading(),
                Heading.MEASURE_ASSOCIATED_CRITERIA.getHeading(),
                Heading.ACB_CERTIFICATION_ID.getHeading(),
                Heading.CERTIFICATION_BODY_NAME.getHeading(),
                Heading.TESTING_LAB_NAME.getHeading(),
                Heading.CERTIFICATION_DATE.getHeading(),
                Heading.DEVELOPER_ADDRESS.getHeading(),
                Heading.DEVELOPER_CITY.getHeading(),
                Heading.DEVELOPER_STATE.getHeading(),
                Heading.DEVELOPER_ZIP.getHeading(),
                Heading.DEVELOPER_WEBSITE.getHeading(),
                Heading.SELF_DEVELOPER.getHeading(),
                Heading.DEVELOPER_EMAIL.getHeading(),
                Heading.DEVELOPER_PHONE.getHeading(),
                Heading.DEVELOPER_CONTACT_NAME.getHeading(),
                Heading.SVAP_NOTICE_URL.getHeading(),
                Heading.RWT_PLANS_URL.getHeading(),
                Heading.RWT_PLANS_CHECK_DATE.getHeading(),
                Heading.RWT_RESULTS_URL.getHeading(),
                Heading.RWT_RESULTS_CHECK_DATE.getHeading(),
                Heading.TARGETED_USERS.getHeading(),
                Heading.QMS_STANDARD_NAME.getHeading(),
                Heading.QMS_STANDARD_APPLICABLE_CRITERIA.getHeading(),
                Heading.QMS_MODIFICATION.getHeading(),
                Heading.ICS.getHeading(),
                Heading.ICS_SOURCE.getHeading(),
                Heading.ACCESSIBILITY_CERTIFIED.getHeading(),
                Heading.ACCESSIBILITY_STANDARD.getHeading(),
                Heading.K_1_URL.getHeading(),
                Heading.CQM_NUMBER.getHeading(),
                Heading.CQM_VERSION.getHeading(),
                Heading.CQM_CRITERIA.getHeading(),
                Heading.SED_REPORT_URL.getHeading(),
                Heading.SED_INTENDED_USERS.getHeading(),
                Heading.SED_TESTING_DATE.getHeading(),
                Heading.PARTICIPANT_ID.getHeading(),
                Heading.PARTICIPANT_GENDER.getHeading(),
                Heading.PARTICIPANT_AGE.getHeading(),
                Heading.PARTICIPANT_EDUCATION.getHeading(),
                Heading.PARTICIPANT_OCCUPATION.getHeading(),
                Heading.PARTICIPANT_PROFESSIONAL_EXPERIENCE.getHeading(),
                Heading.PARTICIPANT_COMPUTER_EXPERIENCE.getHeading(),
                Heading.PARTICIPANT_PRODUCT_EXPERIENCE.getHeading(),
                Heading.PARTICIPANT_ASSISTIVE_TECH.getHeading(),
                Heading.TASK_ID.getHeading(),
                Heading.TASK_DESCRIPTION.getHeading(),
                Heading.TASK_SUCCESS_MEAN.getHeading(),
                Heading.TASK_SUCCESS_STDDEV.getHeading(),
                Heading.TASK_PATH_DEV_OBS.getHeading(),
                Heading.TASK_PATH_DEV_OPT.getHeading(),
                Heading.TASK_TIME_MEAN.getHeading(),
                Heading.TASK_TIME_STDDEV.getHeading(),
                Heading.TASK_TIME_DEV_OBS.getHeading(),
                Heading.TASK_TIME_DEV_OPT.getHeading(),
                Heading.TASK_ERRORS_MEAN.getHeading(),
                Heading.TASK_ERRORS_STDDEV.getHeading(),
                Heading.TASK_RATING_SCALE.getHeading(),
                Heading.TASK_RATING.getHeading(),
                Heading.TASK_RATING_STDDEV.getHeading()
                ).collect(Collectors.toList());

        headings.addAll(getCriteriaHeadings(listing));
        return headings;
    }

    private List<String> getCriteriaHeadings(CertifiedProductSearchDetails listing) {
        List<String> criteriaHeadings = new ArrayList<String>();
        List<CertificationCriterion> allCriteriaAvailableToListing = criteriaManager.getCriteriaAvailableToListing(listing);
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
                criterionHeadings.add(Heading.HAS_ADDITIONAL_SOFTWARE.getHeading());
                criterionHeadings.add(Heading.ADDITIONAL_SOFTWARE_LISTING.getHeading());
                criterionHeadings.add(Heading.ADDITIONAL_SOFTWARE_LISTING_GROUPING.getHeading());
                criterionHeadings.add(Heading.ADDITIONAL_SOFTWARE_NONLISTING.getHeading());
                criterionHeadings.add(Heading.ADDITIONAL_SOFTWARE_NONLISTING_VERSION.getHeading());
                criterionHeadings.add(Heading.ADDITIONAL_SOFTWARE_NONLISTING_GROUPING.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isApiDocumentation()) {
                criterionHeadings.add(Heading.API_DOCUMENTATION_LINK.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isAttestationAnswer()) {
                criterionHeadings.add(Heading.ATTESTATION_ANSWER.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isCodeSet()) {
                criterionHeadings.add(Heading.CODE_SET.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isConformanceMethod()) {
                criterionHeadings.add(Heading.CONFORMANCE_METHOD.getHeading());
                criterionHeadings.add(Heading.CONFORMANCE_METHOD_VERSION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isDocumentationUrl()) {
                criterionHeadings.add(Heading.DOCUMENTATION_URL.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isExportDocumentation()) {
                criterionHeadings.add(Heading.EXPORT_DOCUMENTATION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isFunctionalityTested()) {
                criterionHeadings.add(Heading.FUNCTIONALITIES_TESTED.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isG1Success()) {
                criterionHeadings.add(LegacyHeading.MACRA_MEASURE_G1.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isG2Success()) {
                criterionHeadings.add(LegacyHeading.MACRA_MEASURE_G2.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isGap()) {
                criterionHeadings.add(Heading.GAP.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isOptionalStandard()) {
                criterionHeadings.add(Heading.OPTIONAL_STANDARD.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isPrivacySecurityFramework()) {
                criterionHeadings.add(Heading.PRIVACY_AND_SECURITY.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isRiskManagementSummaryInformation()) {
                criterionHeadings.add(Heading.RISK_MANAGEMENT_SUMMARY_INFORMATION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isSed()) {
                criterionHeadings.add(Heading.UCD_PROCESS.getHeading());
                criterionHeadings.add(Heading.UCD_PROCESS_DETAILS.getHeading());
                criterionHeadings.add(Heading.TASK_ID.getHeading());
                criterionHeadings.add(Heading.PARTICIPANT_ID.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isServiceBaseUrlList()) {
                criterionHeadings.add(Heading.SERVICE_BASE_URL_LIST.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isStandard()
                    //we seem to list all criteria as eligible for standards, so here
                    //we are also limiting the column presence by checking whether the criteria
                    //has any standards available
                    && !CollectionUtils.isEmpty(standardManager.getStandardsByCriteria(criterion.getId()))) {
                criterionHeadings.add(Heading.STANDARD.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isStandardsTested()) {
                criterionHeadings.add(LegacyHeading.TEST_STANDARD.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isSvap()) {
                criterionHeadings.add(Heading.SVAP_REG_TEXT.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isTestData()) {
                criterionHeadings.add(Heading.TEST_DATA.getHeading());
                criterionHeadings.add(Heading.TEST_DATA_VERSION.getHeading());
                criterionHeadings.add(Heading.TEST_DATA_ALTERATION.getHeading());
                criterionHeadings.add(Heading.TEST_DATA_ALTERATION_DESC.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isTestProcedure()) {
                criterionHeadings.add(LegacyHeading.TEST_PROCEDURE.getHeading());
                criterionHeadings.add(LegacyHeading.TEST_PROCEDURE_VERSION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isTestTool()) {
                criterionHeadings.add(Heading.TEST_TOOL_NAME.getHeading());
                criterionHeadings.add(Heading.TEST_TOOL_VERSION.getHeading());
            }
            if (criterionWithAttributes.getAttributes().isUseCases()) {
                criterionHeadings.add(Heading.USE_CASES.getHeading());
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
