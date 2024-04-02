package gov.healthit.chpl.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.ActivityDTO;

@Component
public class ActivityMetadataBuilderFactory {
    private ActivityMetadataBuilder defaultBuilder;
    private FunctionalityTestedActivityMetadataBuilder funcTestedBuilder;
    private StandardActivityMetadataBuilder standardBuilder;
    private SvapActivityMetadataBuilder svapBuilder;
    private ListingActivityMetadataBuilder listingBuilder;
    private DeveloperActivityMetadataBuilder developerBuilder;
    private ProductActivityMetadataBuilder productBuilder;
    private VersionActivityMetadataBuilder versionBuilder;
    private CertificationBodyActivityMetadataBuilder acbBuilder;
    private TestingLabActivityMetadataBuilder atlBuilder;
    private UserMaintenanceActivityMetadataBuilder userMaintenanceActivityMetadataBuilder;
    private ComplaintActivityMetadataBuilder complaintActivityMetadataBuilder;
    private QuarterlyReportActivityMetadataBuilder quarterlyReportActivityMetadataBuilder;
    private AnnualReportActivityMetadataBuilder annualReportActivityMetadataBuilder;
    private ChangeRequestActivityMetadataBuilder changeRequestActivityMetadataBuilder;
    private ApiKeyActivityMetadataBuilder apiKeyBuilder;

    @Autowired
    public ActivityMetadataBuilderFactory (
            @Qualifier("activityMetadataBuilder") ActivityMetadataBuilder defaultBuilder,
            @Qualifier("functionalityTestedActivityMetadataBuilder") FunctionalityTestedActivityMetadataBuilder funcTestedBuilder,
            @Qualifier("standardActivityMetadataBuilder") StandardActivityMetadataBuilder standardBuilder,
            @Qualifier("svapActivityMetadataBuilder") SvapActivityMetadataBuilder svapBuilder,
            @Qualifier("listingActivityMetadataBuilder") ListingActivityMetadataBuilder listingBuilder,
            @Qualifier("developerActivityMetadataBuilder") DeveloperActivityMetadataBuilder developerBuilder,
            @Qualifier("productActivityMetadataBuilder") ProductActivityMetadataBuilder productBuilder,
            @Qualifier("versionActivityMetadataBuilder") VersionActivityMetadataBuilder versionBuilder,
            @Qualifier("acbActivityMetadataBuilder") CertificationBodyActivityMetadataBuilder acbBuilder,
            @Qualifier("atlActivityMetadataBuilder") TestingLabActivityMetadataBuilder atlBuilder,
            @Qualifier("userMaintenanceActivityMetadataBuilder") UserMaintenanceActivityMetadataBuilder userMaintenanceActivityMetadataBuilder,
            @Qualifier("complaintActivityMetadataBuilder") ComplaintActivityMetadataBuilder complaintActivityMetadataBuilder,
            @Qualifier("quarterlyReportActivityMetadataBuilder") QuarterlyReportActivityMetadataBuilder quarterlyReportActivityMetadataBuilder,
            @Qualifier("annualReportActivityMetadataBuilder") AnnualReportActivityMetadataBuilder annualReportActivityMetadataBuilder,
            @Qualifier("changeRequestActivityMetadataBuilder") ChangeRequestActivityMetadataBuilder changeRequestActivityMetadataBuilder,
            @Qualifier("apiKeyActivityMetadataBuilder") ApiKeyActivityMetadataBuilder apiKeyBuilder) {
        this.defaultBuilder = defaultBuilder;
        this.funcTestedBuilder = funcTestedBuilder;
        this.standardBuilder = standardBuilder;
        this.svapBuilder = svapBuilder;
        this.listingBuilder = listingBuilder;
        this.developerBuilder = developerBuilder;
        this.productBuilder = productBuilder;
        this.versionBuilder = versionBuilder;
        this.acbBuilder = acbBuilder;
        this.atlBuilder = atlBuilder;
        this.userMaintenanceActivityMetadataBuilder = userMaintenanceActivityMetadataBuilder;
        this.complaintActivityMetadataBuilder = complaintActivityMetadataBuilder;
        this.quarterlyReportActivityMetadataBuilder = quarterlyReportActivityMetadataBuilder;
        this.annualReportActivityMetadataBuilder = annualReportActivityMetadataBuilder;
        this.changeRequestActivityMetadataBuilder = changeRequestActivityMetadataBuilder;
        this.apiKeyBuilder = apiKeyBuilder;
    }

    public ActivityMetadataBuilder getBuilder(ActivityDTO dto) {
        ActivityMetadataBuilder builder = null;
        switch (dto.getConcept()) {
        case CERTIFIED_PRODUCT:
            builder = listingBuilder;
            break;
        case DEVELOPER:
            builder = developerBuilder;
            break;
        case PRODUCT:
            builder = productBuilder;
            break;
        case VERSION:
            builder = versionBuilder;
            break;
        case CERTIFICATION_BODY:
            builder = acbBuilder;
            break;
        case TESTING_LAB:
            builder = atlBuilder;
            break;
        case USER:
            builder = userMaintenanceActivityMetadataBuilder;
            break;
        case COMPLAINT:
            builder = complaintActivityMetadataBuilder;
            break;
        case QUARTERLY_REPORT:
        case QUARTERLY_REPORT_LISTING:
            builder = quarterlyReportActivityMetadataBuilder;
            break;
        case ANNUAL_REPORT:
            builder = annualReportActivityMetadataBuilder;
            break;
        case CHANGE_REQUEST:
            builder = changeRequestActivityMetadataBuilder;
            break;
        case FUNCTIONALITY_TESTED:
            builder = funcTestedBuilder;
            break;
        case STANDARD:
            builder = standardBuilder;
            break;
        case SVAP:
            builder = svapBuilder;
            break;
        case API_KEY:
            builder = apiKeyBuilder;
            break;
        case ANNOUNCEMENT:
        case CORRECTIVE_ACTION_PLAN:
        case PENDING_SURVEILLANCE:
            builder = defaultBuilder;
            break;
        default:
            break;
        }
        return builder;
    }
}
