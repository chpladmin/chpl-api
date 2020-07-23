package gov.healthit.chpl.caching;

public final class CacheNames {
    private CacheNames() {}

    public static final String ALL_CERT_IDS_WITH_PRODUCTS = "allCertIdsWithProducts";
    public static final String ALL_CERT_IDS = "allCertIds";
    public static final String ALL_DEVELOPERS = "allDevelopers";
    public static final String ALL_DEVELOPERS_INCLUDING_DELETED = "allDevelopersIncludingDeleted";
    public static final String JOB_TYPES = "jobTypes";
    public static final String EDITIONS = "editions";
    public static final String EDITION_NAMES = "editionNames";
    public static final String CERTIFICATION_STATUSES = "certificationStatuses";
    public static final String PRACTICE_TYPE_NAMES = "practiceTypeNames";
    public static final String CLASSIFICATION_NAMES = "classificationNames";
    public static final String PRODUCT_NAMES = "productNames";
    public static final String DEVELOPER_NAMES = "developerNames";
    public static final String MACRA_MEASURES = "macrameasures";
    public static final String CQM_CRITERION = "cqmCriterion";
    public static final String CQM_CRITERION_NUMBERS = "cqmCriterionNumbers";
    public static final String CERTIFICATION_CRITERION_NUMBERS = "certificationCriterionNumbers";
    public static final String CERTIFICATION_CRITERION_WITH_EDITIONS = "certificationCriterionWithEditions";
    public static final String GET_DECERTIFIED_DEVELOPERS = "getDecertifiedDevelopers";
    public static final String GET_ALL_UNRESTRICTED_APIKEYS = "getAllUnrestrictedApiKeys";
    public static final String FIND_SURVEILLANCE_REQ_TYPE = "findSurveillanceRequirementType";
    public static final String FIND_SURVEILLANCE_RESULT_TYPE = "findSurveillanceResultType";
    public static final String FIND_SURVEILLANCE_NONCONFORMITY_STATUS_TYPE = "findSurveillanceNonconformityStatusType";
    public static final String UPLOAD_TEMPLATE_VERSIONS = "uploadTemplateVersions";
    public static final String TEST_PROCEDURES = "testProcedures";
    public static final String TEST_DATA = "testData";
    public static final String TEST_FUNCTIONALITY_MAPS = "testFunctionalityMaps";
    public static final String COLLECTIONS_DEVELOPERS = "developerCollection";
    public static final String COLLECTIONS_LISTINGS = "listingCollection";

    //caches that are pre-fetched due to longish load times
    public static final String PREFETCHED_COLLECTIONS_LISTINGS = "prefetchedListingCollection";
    public static final String PREFETCHED_ALL_CERT_IDS = "prefetchedAllCertIds";
    public static final String PREFETCHED_ALL_CERT_IDS_WITH_PRODUCTS = "prefetchedAllCertIdsWithProducts";
    public static final String PREFETCHED_PRODUCT_NAMES = "prefetchedProductNames";
    public static final String PREFETCHED_DEVELOPER_NAMES = "prefetchedDeveloperNames";
}
