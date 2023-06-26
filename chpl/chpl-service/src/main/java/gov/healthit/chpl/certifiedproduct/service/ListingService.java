package gov.healthit.chpl.certifiedproduct.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.compliance.directreview.DirectReviewComparator;
import gov.healthit.chpl.compliance.directreview.DirectReviewSearchService;
import gov.healthit.chpl.compliance.surveillance.SurveillanceManager;
import gov.healthit.chpl.dao.CertifiedProductAccessibilityStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductChplProductNumberHistoryDao;
import gov.healthit.chpl.dao.CertifiedProductQmsStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.CertifiedProductTargetedUserDAO;
import gov.healthit.chpl.dao.CertifiedProductTestingLabDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductChplProductNumberHistory;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.PromotingInteroperabilityUser;
import gov.healthit.chpl.domain.comparator.CertificationCriterionComparator;
import gov.healthit.chpl.domain.comparator.CertifiedProductAccessibilityStandardComparator;
import gov.healthit.chpl.domain.comparator.CertifiedProductComparator;
import gov.healthit.chpl.domain.comparator.CertifiedProductQmsStandardComparator;
import gov.healthit.chpl.domain.comparator.CertifiedProductTargetedUserComparator;
import gov.healthit.chpl.domain.comparator.CertifiedProductTestingLabComparator;
import gov.healthit.chpl.domain.comparator.CertifiedProductUcdProcessComparator;
import gov.healthit.chpl.domain.comparator.ChplProductNumberHistoryComparator;
import gov.healthit.chpl.domain.comparator.TestParticipantComparator;
import gov.healthit.chpl.domain.comparator.TestTaskComparator;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ListingService {

    private CertificationResultService certificationResultService;
    private ListingMeasuresService listingMeasureService;
    private CqmResultsService cqmResultsService;
    private CertificationStatusEventsService certificationStatusEventsService;
    private DirectReviewSearchService drService;
    private PromotingInteroperabilityUserHistoryService piuService;

    private ChplProductNumberUtil chplProductNumberUtil;
    private DimensionalDataManager dimensionalDataManager;
    private SurveillanceManager survManager;

    private CertifiedProductTestingLabDAO certifiedProductTestingLabDao;
    private ListingGraphDAO listingGraphDao;
    private CertifiedProductChplProductNumberHistoryDao chplProductNumberHistoryDao;
    private CertifiedProductQmsStandardDAO certifiedProductQmsStandardDao;
    private CertifiedProductTargetedUserDAO certifiedProductTargetedUserDao;
    private CertifiedProductAccessibilityStandardDAO certifiedProductAsDao;
    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    private CertificationCriterionComparator criteriaComparator;

    private CertifiedProductComparator cpComparator;
    private CertifiedProductTestingLabComparator atlComparator;
    private CertifiedProductQmsStandardComparator qmsComparator;
    private CertifiedProductTargetedUserComparator tuComparator;
    private CertifiedProductAccessibilityStandardComparator asComparator;
    private ChplProductNumberHistoryComparator chplProductNumberHistoryComparator;
    private DirectReviewComparator drComparator;
    private CertifiedProductUcdProcessComparator ucdComparator;
    private TestTaskComparator ttComparator;
    private TestParticipantComparator tpComparator;

    @SuppressWarnings("checkstyle:parameternumber")
    @Autowired
    public ListingService(
            CertificationResultService certificationResultService,
            ListingMeasuresService listingMeasureService,
            CqmResultsService cqmResultsService,
            CertificationStatusEventsService certificationStatusEventsService,
            DirectReviewSearchService drService,
            PromotingInteroperabilityUserHistoryService piuService,
            ChplProductNumberUtil chplProductNumberUtil,
            DimensionalDataManager dimensionalDataManager,
            SurveillanceManager survManager,
            CertifiedProductTestingLabDAO certifiedProductTestingLabDao,
            ListingGraphDAO listingGraphDao,
            @Qualifier("certifiedProductChplProductNumberHistoryDao")
            CertifiedProductChplProductNumberHistoryDao chplProductNumberHistoryDao,
            CertifiedProductQmsStandardDAO certifiedProductQmsStandardDao,
            CertifiedProductTargetedUserDAO certifiedProductTargetedUserDao,
            CertifiedProductAccessibilityStandardDAO certifiedProductAsDao,
            CertifiedProductSearchResultDAO certifiedProductSearchResultDAO,
            CertificationCriterionComparator criteriaComparator) {

        this.certificationResultService = certificationResultService;
        this.listingMeasureService = listingMeasureService;
        this.cqmResultsService = cqmResultsService;
        this.certificationStatusEventsService = certificationStatusEventsService;
        this.drService = drService;
        this.piuService = piuService;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.dimensionalDataManager = dimensionalDataManager;
        this.survManager = survManager;
        this.certifiedProductTestingLabDao = certifiedProductTestingLabDao;
        this.listingGraphDao = listingGraphDao;
        this.chplProductNumberHistoryDao = chplProductNumberHistoryDao;
        this.certifiedProductQmsStandardDao = certifiedProductQmsStandardDao;
        this.certifiedProductTargetedUserDao = certifiedProductTargetedUserDao;
        this.certifiedProductAsDao = certifiedProductAsDao;
        this.certifiedProductSearchResultDAO = certifiedProductSearchResultDAO;
        this.criteriaComparator = criteriaComparator;

        this.cpComparator = new CertifiedProductComparator();
        this.atlComparator = new CertifiedProductTestingLabComparator();
        this.qmsComparator = new CertifiedProductQmsStandardComparator();
        this.tuComparator = new CertifiedProductTargetedUserComparator();
        this.asComparator = new CertifiedProductAccessibilityStandardComparator();
        this.chplProductNumberHistoryComparator = new ChplProductNumberHistoryComparator();
        this.drComparator = new DirectReviewComparator();
        this.ucdComparator = new CertifiedProductUcdProcessComparator();
        this.ttComparator = new TestTaskComparator();
        this.tpComparator = new TestParticipantComparator();
    }

    public CertifiedProductSearchDetails createCertifiedSearchDetails(Long listingId) throws EntityRetrievalException {

        CertifiedProductDetailsDTO dto = certifiedProductSearchResultDAO.getById(listingId);
        CertifiedProductSearchDetails searchDetails = createCertifiedProductSearchDetailsWithBasicDataOnly(certifiedProductSearchResultDAO.getById(listingId));

        searchDetails.setCertificationResults(certificationResultService.getCertificationResults(searchDetails));
        searchDetails.setCqmResults(cqmResultsService.getCqmResultDetails(dto.getId(), dto.getYear()));
        sortSed(searchDetails);

        // get first-level parents and children
        searchDetails.getIcs().setParents(populateRelatedCertifiedProducts(getCertifiedProductParents(dto.getId())));
        searchDetails.getIcs().setChildren(populateRelatedCertifiedProducts(getCertifiedProductChildren(dto.getId())));
        return searchDetails;
    }

    public CertifiedProductSearchDetails createCertifiedProductSearchDetailsBasic(Long listingId) throws EntityRetrievalException {
        CertifiedProductDetailsDTO dto = certifiedProductSearchResultDAO.getById(listingId);
        return createCertifiedProductSearchDetailsWithBasicDataOnly(dto);
    }

    public CertifiedProductSearchDetails createCertifiedProductSearchDetailsWithBasicDataOnly(CertifiedProductDetailsDTO dto) throws EntityRetrievalException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(dto.getId())
                .acbCertificationId(dto.getAcbCertificationId())
                .certificationDate(dto.getCertificationDate() != null ? dto.getCertificationDate().getTime() : null)
                .decertificationDate(dto.getDecertificationDate() != null ? dto.getDecertificationDate().getTime() : null)
                .curesUpdate(dto.getCuresUpdate())
                .certificationEdition(getCertificationEdition(dto))
                .chplProductNumber(getChplProductNumber(dto))
                .certifyingBody(getCertifyingBody(dto))
                .classificationType(getClassificationType(dto))
                .otherAcb(dto.getOtherAcb())
                .practiceType(getPracticeType(dto))
                .reportFileLocation(dto.getReportFileLocation())
                .sedReportFileLocation(dto.getSedReportFileLocation())
                .sedIntendedUserDescription(dto.getSedIntendedUserDescription())
                .sedTestingEndDay(dto.getSedTestingEnd())
                .testingLabs(getTestingLabs(dto.getId()))
                .developer(dto.getDeveloper())
                .product(dto.getProduct())
                .version(new ProductVersion(dto.getVersion()))
                .productAdditionalSoftware(dto.getProductAdditionalSoftware())
                .mandatoryDisclosures(dto.getMandatoryDisclosures())
                .lastModifiedDate(dto.getLastModifiedDate().getTime())
                .countCerts(dto.getCountCertifications())
                .countCqms(dto.getCountCqms())
                .countSurveillance(dto.getCountSurveillance())
                .countOpenSurveillance(dto.getCountOpenSurveillance())
                .countClosedSurveillance(dto.getCountClosedSurveillance())
                .countOpenNonconformities(dto.getCountOpenNonconformities())
                .countClosedNonconformities(dto.getCountClosedNonconformities())
                .surveillance(survManager.getByCertifiedProduct(dto.getId()))
                .chplProductNumberHistory(getCertifiedProductChplProductNumberHistory(dto.getId()))
                .qmsStandards(getCertifiedProductQmsStandards(dto.getId()))
                .measures(listingMeasureService.getCertifiedProductMeasures(dto.getId(), false))
                .targetedUsers(getCertifiedProductTargetedUsers(dto.getId()))
                .accessibilityStandards(getCertifiedProductAccessibilityStandards(dto.getId()))
                .rwtPlansUrl(dto.getRwtPlansUrl())
                .rwtPlansCheckDate(dto.getRwtPlansCheckDate())
                .rwtResultsUrl(dto.getRwtResultsUrl())
                .rwtResultsCheckDate(dto.getRwtResultsCheckDate())
                .svapNoticeUrl(dto.getSvapNoticeUrl())
                .sed(new CertifiedProductSed())
                .build();

        List<PromotingInteroperabilityUser> promotingInteroperabilityUserHistory = piuService.getPromotingInteroperabilityUserHistory(dto.getId());
        listing.setPromotingInteroperabilityUserHistory(promotingInteroperabilityUserHistory);

        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setInherits(dto.getIcs());
        listing.setIcs(ics);
        //cannot put this in the builder method because it's immutable meaning we can't sort it later
        listing.setCertificationEvents(certificationStatusEventsService.getCertificationStatusEvents(dto.getId()));
        populateDirectReviews(listing);
        return listing;
    }

    private void sortSed(CertifiedProductSearchDetails searchDetails) {
        if (searchDetails.getSed() != null && !CollectionUtils.isEmpty(searchDetails.getSed().getUcdProcesses())) {
            searchDetails.getSed().setUcdProcesses(searchDetails.getSed().getUcdProcesses().stream()
                .sorted(ucdComparator)
                .collect(Collectors.toList()));
            searchDetails.getSed().getUcdProcesses().stream()
                .forEach(ucd -> ucd.setCriteria(ucd.getCriteria().stream()
                    .sorted(criteriaComparator)
                    .collect(Collectors.toCollection(LinkedHashSet::new))));
        }

        if (searchDetails.getSed() != null && !CollectionUtils.isEmpty(searchDetails.getSed().getTestTasks())) {
            searchDetails.getSed().setTestTasks(searchDetails.getSed().getTestTasks().stream()
                    .sorted(ttComparator)
                    .collect(Collectors.toList()));
                searchDetails.getSed().getTestTasks().stream()
                    .forEach(tt -> tt.setCriteria(tt.getCriteria().stream()
                        .sorted(criteriaComparator)
                        .collect(Collectors.toCollection(LinkedHashSet::new))));
                searchDetails.getSed().getTestTasks().stream()
                    .forEach(tt -> tt.setTestParticipants(tt.getTestParticipants().stream()
                        .sorted(tpComparator)
                        .collect(Collectors.toCollection(LinkedHashSet::new))));
        }
    }

    private List<CertifiedProductTestingLab> getTestingLabs(Long listingId) throws EntityRetrievalException {
        return certifiedProductTestingLabDao.getTestingLabsByCertifiedProductId(listingId).stream()
                .map(dto -> new CertifiedProductTestingLab(dto))
                .sorted(atlComparator)
                .collect(Collectors.toList());
    }

    private void populateDirectReviews(CertifiedProductSearchDetails listing) {
        List<DirectReview> drs = new ArrayList<DirectReview>();
        if (listing.getDeveloper() != null && listing.getDeveloper().getId() != null) {
            drs = drService.getDirectReviewsRelatedToListing(listing.getId(),
                    listing.getDeveloper().getId(),
                    MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY),
                    listing.getCertificationEvents(), LOGGER);
        }
        listing.setDirectReviews(drs.stream()
                .sorted(drComparator)
                .collect(Collectors.toList()));
        listing.setDirectReviewsAvailable(drService.doesCacheHaveAnyOkData());
    }

    private List<CertifiedProductDTO> getCertifiedProductChildren(Long id) {
        List<CertifiedProductDTO> children = listingGraphDao.getChildren(id);
        return children;
    }

    private List<CertifiedProductDTO> getCertifiedProductParents(Long id) {
        List<CertifiedProductDTO> parents = listingGraphDao.getParents(id);
        return parents;
    }

    private List<CertifiedProduct> populateRelatedCertifiedProducts(List<CertifiedProductDTO> relatedCertifiedProductDTOs) throws EntityRetrievalException {

        return relatedCertifiedProductDTOs.stream()
                .map(dto -> createCertifiedProductBasedOnDto(dto))
                .sorted(cpComparator)
                .collect(Collectors.toList());
    }

    private CertifiedProduct createCertifiedProductBasedOnDto(CertifiedProductDTO dto) {
        CertifiedProduct cp = new CertifiedProduct();
        cp.setId(dto.getId());
        cp.setChplProductNumber(chplProductNumberUtil.generate(dto.getId()));
        cp.setLastModifiedDate(dto.getLastModifiedDate() != null ? dto.getLastModifiedDate().getTime() : null);
        CertificationEdition edition = getEdition(dto.getCertificationEditionId());
        if (edition != null) {
            cp.setEdition(edition.getYear());
        }
        CertificationStatusEvent cse = certificationStatusEventsService.getInitialCertificationEvent(dto.getId());
        if (cse != null) {
            cp.setCertificationDate(cse.getEventDate());
        } else {
            cp.setCertificationDate(-1);
        }
        return cp;
    }

    private CertificationEdition getEdition(Long editionId) {
        Optional<CertificationEdition> certEdition = dimensionalDataManager.getCertificationEditions().stream()
                .filter(ed -> ed.getCertificationEditionId().equals(editionId))
                .findAny();

        return certEdition.orElse(null);
    }

    private String getChplProductNumber(CertifiedProductDetailsDTO dto) {
        if (!StringUtils.isEmpty(dto.getChplProductNumber())) {
            return dto.getChplProductNumber();
        } else {
            return chplProductNumberUtil.generate(dto.getId());
        }
    }

    private Map<String, Object> getCertificationEdition(CertifiedProductDetailsDTO dto) {
        Map<String, Object> certificationEdition = new HashMap<String, Object>();
        certificationEdition.put("id", dto.getCertificationEditionId());
        certificationEdition.put("name", dto.getYear());
        return certificationEdition;
    }

    private Map<String, Object> getCertifyingBody(CertifiedProductDetailsDTO dto) {
        Map<String, Object> certifyingBody = new HashMap<String, Object>();
        certifyingBody.put("id", dto.getCertificationBodyId());
        certifyingBody.put("name", dto.getCertificationBodyName());
        certifyingBody.put("code", dto.getCertificationBodyCode());
        return certifyingBody;
    }

    private Map<String, Object> getClassificationType(CertifiedProductDetailsDTO dto) {
        Map<String, Object> classificationType = new HashMap<String, Object>();
        classificationType.put("id", dto.getProductClassificationTypeId());
        classificationType.put("name", dto.getProductClassificationName());
        return classificationType;
    }

    private Map<String, Object> getPracticeType(CertifiedProductDetailsDTO dto) {
        Map<String, Object> practiceType = new HashMap<String, Object>();
        practiceType.put("id", dto.getPracticeTypeId());
        practiceType.put("name", dto.getPracticeTypeName());
        return practiceType;
    }

    private List<CertifiedProductChplProductNumberHistory> getCertifiedProductChplProductNumberHistory(Long id) throws EntityRetrievalException {
        return chplProductNumberHistoryDao.getHistoricalChplProductNumbers(id).stream()
                .sorted(chplProductNumberHistoryComparator)
                .toList();
    }

    private List<CertifiedProductQmsStandard> getCertifiedProductQmsStandards(Long id) throws EntityRetrievalException {
        return certifiedProductQmsStandardDao.getQmsStandardsByCertifiedProductId(id).stream()
                .map(dto -> new CertifiedProductQmsStandard(dto))
                .sorted(qmsComparator)
                .collect(Collectors.toList());
    }

    private List<CertifiedProductTargetedUser> getCertifiedProductTargetedUsers(Long id) throws EntityRetrievalException {
        return certifiedProductTargetedUserDao.getTargetedUsersByCertifiedProductId(id).stream()
                .map(dto -> new CertifiedProductTargetedUser(dto))
                .sorted(tuComparator)
                .collect(Collectors.toList());
    }

    private List<CertifiedProductAccessibilityStandard> getCertifiedProductAccessibilityStandards(Long id) throws EntityRetrievalException {
        return certifiedProductAsDao.getAccessibilityStandardsByCertifiedProductId(id).stream()
                .map(dto -> new CertifiedProductAccessibilityStandard(dto))
                .sorted(asComparator)
                .collect(Collectors.toList());
    }
}
