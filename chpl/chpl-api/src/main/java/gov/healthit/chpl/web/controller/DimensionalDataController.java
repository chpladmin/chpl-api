package gov.healthit.chpl.web.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.complaint.ComplaintManager;
import gov.healthit.chpl.complaint.domain.ComplainantType;
import gov.healthit.chpl.complaint.domain.ComplaintType;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CriteriaSpecificDescriptiveModel;
import gov.healthit.chpl.domain.DimensionalData;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.domain.SearchOption;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceCapStatus;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceGroundsForInitiating;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceOutcome;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceProcessType;
import gov.healthit.chpl.svap.manager.SvapManager;
import gov.healthit.chpl.testprocedure.TestProcedureManager;
import gov.healthit.chpl.teststandard.TestStandard;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.CacheControl;
import gov.healthit.chpl.web.controller.annotation.CacheMaxAge;
import gov.healthit.chpl.web.controller.annotation.CachePolicy;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApi;
import gov.healthit.chpl.web.controller.results.SvapResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Deprecated
@Tag(name = "dimensional-data", description = "Access lookup data.")
@RestController
@RequestMapping("/data")
public class DimensionalDataController {
    private DimensionalDataManager dimensionalDataManager;
    private TestProcedureManager tpManager;
    private ComplaintManager complaintManager;
    private SurveillanceReportManager survReportManager;
    private ChangeRequestManager changeRequestManager;
    private SvapManager svapManager;

    @Autowired
    public DimensionalDataController(DimensionalDataManager dimensionalDataManager,
            TestProcedureManager tpManager,
            ComplaintManager complaintManager,
            SurveillanceReportManager survReportManager,
            ChangeRequestManager changeRequestManager,
            SvapManager svapManager) {
        this.dimensionalDataManager = dimensionalDataManager;
        this.complaintManager = complaintManager;
        this.survReportManager = survReportManager;
        this.changeRequestManager = changeRequestManager;
        this.svapManager = svapManager;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/quarters",
        message = "This is deprecated and will be removed. Please GET from /surveillance-report/quarters.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get a list of quarters for which a surveillance report can be created.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/quarters", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getQuarters() {
        return dimensionalDataManager.getQuarters();
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/surveillance-process-types",
        message = "This is deprecated and will be removed. Please GET from /surveillance-report/surveillance-process-types.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get a list of surveillance process types.",
            description = "Security Restrictions: Users with either role chpl-admin, chpl-onc, or chpl-onc-acb",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/surveillance-process-types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getSurveillanceProcessTypes() {
        List<SurveillanceProcessType> spts = survReportManager.getSurveillanceProcessTypes();
        return spts.stream()
                .map(spt -> new KeyValueModel(spt.getId(), spt.getName()))
                .collect(Collectors.toSet());
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/surveillance-outcomes",
        message = "This is deprecated and will be removed. Please GET from /surveillance-report/surveillance-outcomes.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get a list of surveillance outcomes.",
            description = "Security Restrictions: Users with either role chpl-admin, chpl-onc, or chpl-onc-acb",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/surveillance-outcomes", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getSurveillanceOutcomes() {
        List<SurveillanceOutcome> outcomes = survReportManager.getSurveillanceOutcomes();
        return outcomes.stream()
                .map(outcome -> new KeyValueModel(outcome.getId(), outcome.getName()))
                .collect(Collectors.toSet());
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/surveillance-grounds-for-initiating",
        message = "This is deprecated and will be removed. Please GET from /surveillance-report/surveillance-grounds-for-initiating.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get a list of options for grounds for initiating surveillance.",
            description = "Security Restrictions: Users with either role chpl-admin, chpl-onc, or chpl-onc-acb",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/surveillance-grounds-for-initiating", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getSurveillanceGroundsForInitiating() {
        List<SurveillanceGroundsForInitiating> grounds = survReportManager.getSurveillanceGroundsForInitiating();
        return grounds.stream()
                .map(ground -> new KeyValueModel(ground.getId(), ground.getName()))
                .collect(Collectors.toSet());
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/cap-statuses",
        message = "This is deprecated and will be removed. Please GET from /surveillance-report/cap-statuses.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get a list of options for Corrective Action Plan (CAP) status values.",
            description = "Security Restrictions: Users with either role chpl-admin, chpl-onc, or chpl-onc-acb",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/cap-statuses", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getSurveillanceCapStatuses() {
        List<SurveillanceCapStatus> capStatuses = survReportManager.getSurveillanceCapStatuses();
        return capStatuses.stream()
                .map(cs -> new KeyValueModel(cs.getId(), cs.getName()))
                .collect(Collectors.toSet());
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/classification_types",
        message = "This is deprecated and will be removed.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible classifications in the CHPL",
            description = "This is useful for knowing what values one might possibly search for.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/classification_types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getClassificationNames() {
        return dimensionalDataManager.getClassificationNames();
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/certification_editions",
        message = "This is deprecated and will be removed.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible certificaiton editions in the CHPL",
            description = "This is useful for knowing what values one might possibly search for.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/certification_editions", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getEditionNames() {
        return dimensionalDataManager.getEditionNames(false);
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/certification_statuses",
        message = "This is deprecated and will be removed. Please use /certified_products/certification-statuses",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible certification statuses in the CHPL",
            description = "This is useful for knowing what values one might possibly search for.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/certification_statuses", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getCertificationStatuses() {
        return dimensionalDataManager.getCertificationStatuses();
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/practice_types",
        message = "This is deprecated and will be removed.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible practice types in the CHPL",
            description = "This is useful for knowing what values one might possibly search for.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/practice_types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getPracticeTypeNames() {
        return dimensionalDataManager.getPracticeTypeNames();
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/certification_bodies",
        message = "This is deprecated and will be removed.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible ACBs in the CHPL",
            description = "This is useful for knowing what values one might possibly search for.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/certification_bodies", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody Set<CertificationBody> getCertificationBodies() {
        return dimensionalDataManager.getAllAcbs();
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/education_types",
        message = "This is deprecated and will be removed. Please GET from /sed/education-types.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible education types in the CHPL",
            description = "This is useful for knowing what values one might possibly search for.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/education_types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getEducationTypes() {
        Set<KeyValueModel> data = dimensionalDataManager.getEducationTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/age_ranges",
        message = "This is deprecated and will be removed. Please GET from /sed/age-ranges.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible test participant age ranges in the CHPL",
            description = "This is useful for knowing what values one might possibly search for.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/age_ranges", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getAgeRanges() {
        Set<KeyValueModel> data = dimensionalDataManager.getAgeRanges();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/test_procedures",
        message = "This is deprecated and will be removed. Please GET from /test-procedures.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible test procedure options in the CHPL",
            description = "This is useful for knowing what values one might possibly search for.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/test_procedures", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getTestProcedures() {
        Set<CriteriaSpecificDescriptiveModel> data = tpManager.getAllWithMappedCriteria().stream()
                .map(tp -> {
                    CriteriaSpecificDescriptiveModel model = new CriteriaSpecificDescriptiveModel();
                    model.setId(tp.getTestProcedureId());
                    model.setName(tp.getTestProcedure().getName());
                    model.setCriteria(tp.getCriteria());
                    return model;
                })
                .collect(Collectors.toSet());
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/test_data", message = "This is deprecated and will be removed. Please GET from /test-data.",
        removalDate = "2025-10-01")
    @Operation(summary = "Get all possible test data options in the CHPL",
            description = "This is useful for knowing what values one might possibly search for.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/test_data", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getTestData() {
        Set<CriteriaSpecificDescriptiveModel> data = dimensionalDataManager.getTestData();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/test_standards",
        message = "This is deprecated and will be removed. Please GET from /test-standards.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible test standard options in the CHPL",
            description = "This is useful for knowing what values one might possibly search for.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/test_standards", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getTestStandards() {
        Set<TestStandard> data = dimensionalDataManager.getTestStandards();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/targeted_users",
        message = "This is deprecated and will be removed. Please GET from /targeted-users.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible targeted user options in the CHPL",
            description = "This is useful for knowing what values one might possibly search for.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/targeted_users", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getTargetedUsers() {
        Set<KeyValueModel> data = dimensionalDataManager.getTargetedUesrs();
        SearchOption result = new SearchOption();
        result.setExpandable(true);
        result.setData(data);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/measures",
        message = "This is deprecated and will be removed. Please GET from /measures.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible measure options in the CHPL",
            description = "This is useful for knowing what values one might possibly search for.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/measures", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getMeasures() {
        Set<Measure> data = dimensionalDataManager.getMeasures();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/measure-types",
        message = "This is deprecated and will be removed. Please GET from /measures/measure-types.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible types of measures in the CHPL, currently this is G1 and G2.",
            description = "This is useful for knowing what values one might possibly search for.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/measure-types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getMeasureTypes() {
        Set<MeasureType> data = dimensionalDataManager.getMeasureTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/developer_statuses",
        message = "This is deprecated and will be removed. Please GET from /developers/statuses.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible developer status options in the CHPL",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/developer_statuses", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getDeveloperStatuses() {
        Set<KeyValueModel> data = dimensionalDataManager.getDeveloperStatuses();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/surveillance_types",
        message = "This is deprecated and will be removed. Please GET from /surveillance/types.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible surveillance type options in the CHPL",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/surveillance_types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getSurveillanceTypes() {
        Set<KeyValueModel> data = dimensionalDataManager.getSurveillanceTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/surveillance_result_types",
        message = "This is deprecated and will be removed. Please GET from /surveillance/result-types.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible surveillance result type options in the CHPL",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/surveillance_result_types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getSurveillanceResultTypes() {
        Set<KeyValueModel> data = dimensionalDataManager.getSurveillanceResultTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/requirement-group-types",
        message = "This is deprecated and will be removed. Please GET from /surveillance/requirement-group-types.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible requirement group type options in the CHPL",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/requirement-group-types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getRequirementGroupTypes() {
        Set<KeyValueModel> data = dimensionalDataManager.getRequirementGroupTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/requirement-types",
        message = "This is deprecated and will be removed. Please GET from /surveillance/requirement-types.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible surveillance requirement detail type options in the CHPL",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/requirement-types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getRequirementTypes() {
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(dimensionalDataManager.getRequirementTypes());
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/nonconformity-types/v2",
        message = "This is deprecated and will be removed. Please GET from /surveillance/non-conformity-types.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible nonconformity type options in the CHPL",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/nonconformity-types/v2", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getNonconformityTypes() {
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(dimensionalDataManager.getNonconformityTypes());
        return result;
    }

    @Operation(summary = "Get all search options in the CHPL",
            description = "This returns all of the other /data/{something} results in one single response.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/search-options", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody DimensionalData getSearchOptions(
            @RequestParam(value = "simple", required = false, defaultValue = "false") Boolean simple)
            throws EntityRetrievalException {
        return dimensionalDataManager.getDimensionalData(simple);
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/complaint-types",
        message = "This is deprecated and will be removed. Please GET from /complaints/types.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible complaint types in the CHPL",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/complaint-types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getComplaintTypes() {
        List<ComplaintType> complaintTypes = complaintManager.getComplaintTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();
        for (ComplaintType complaintType : complaintTypes) {
            results.add(new KeyValueModel(complaintType.getId(), complaintType.getName()));
        }
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(results);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/complainant-types",
        message = "This is deprecated and will be removed. Please GET from /complaints/complainant-types.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible complainant types in the CHPL",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/complainant-types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getComplainantTypes() {
        List<ComplainantType> complainantTypes = complaintManager.getComplainantTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();
        for (ComplainantType complainantType : complainantTypes) {
            results.add(new KeyValueModel(complainantType.getId(), complainantType.getName()));
        }
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(results);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/change-request-types",
        message = "This is deprecated and will be removed. Please GET from /change-requests/types.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible change request types in the CHPL",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/change-request-types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getChangeRequestTypes() {
        List<ChangeRequestType> crTypes = changeRequestManager.getChangeRequestTypes();
        Set<KeyValueModel> data = crTypes.stream()
            .map(crType -> new KeyValueModel(crType.getId(), crType.getName()))
            .collect(Collectors.<KeyValueModel>toSet());
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/change-request-status-types",
        message = "This is deprecated and will be removed. Please GET from /change-requests/status-types.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible change request status types in the CHPL",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/change-request-status-types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getChangeRequestStatusTypes() {
        List<ChangeRequestStatusType> crStatusTypes = changeRequestManager.getChangeRequestStatusTypes();
        Set<KeyValueModel> data = crStatusTypes.stream()
                .map(crType -> new KeyValueModel(crType.getId(), crType.getName()))
                .collect(Collectors.<KeyValueModel>toSet());
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/data/svaps",
        message = "This is deprecated and will be removed.",
        removalDate = "2025-12-31")
    @Operation(summary = "Get all possible SVAP and associated criteria in the CHPL",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/svap", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SvapResults getSvapCriteriaMaps() throws EntityRetrievalException {
        return new SvapResults(svapManager.getAllSvapCriteriaMaps());
    }
}
