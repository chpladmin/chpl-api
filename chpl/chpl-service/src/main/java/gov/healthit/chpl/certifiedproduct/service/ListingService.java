package gov.healthit.chpl.certifiedproduct.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertifiedProductAccessibilityStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductQmsStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.CertifiedProductTargetedUserDAO;
import gov.healthit.chpl.dao.CertifiedProductTestingLabDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.TransparencyAttestation;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.dto.CertificationStatusEventDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.service.DirectReviewSearchService;
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
    private MeaningfulUseUserHistoryService meaningfulUseUserHistoryService;

    private ChplProductNumberUtil chplProductNumberUtil;
    private DimensionalDataManager dimensionalDataManager;
    private SurveillanceManager survManager;

    private CertifiedProductTestingLabDAO certifiedProductTestingLabDao;
    private ListingGraphDAO listingGraphDao;
    private CertifiedProductQmsStandardDAO certifiedProductQmsStandardDao;
    private CertifiedProductTargetedUserDAO certifiedProductTargetedUserDao;
    private CertifiedProductAccessibilityStandardDAO certifiedProductAsDao;
    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;

    @SuppressWarnings("checkstyle:parameternumber")
    @Autowired
    public ListingService(
            CertificationResultService certificationResultService,
            ListingMeasuresService listingMeasureService,
            CqmResultsService cqmResultsService,
            CertificationStatusEventsService certificationStatusEventsService,
            DirectReviewSearchService drService,
            MeaningfulUseUserHistoryService meaningfulUseUserHistoryService,
            ChplProductNumberUtil chplProductNumberUtil,
            DimensionalDataManager dimensionalDataManager,
            SurveillanceManager survManager,
            CertifiedProductTestingLabDAO certifiedProductTestingLabDao,
            ListingGraphDAO listingGraphDao,
            CertifiedProductQmsStandardDAO certifiedProductQmsStandardDao,
            CertifiedProductTargetedUserDAO certifiedProductTargetedUserDao,
            CertifiedProductAccessibilityStandardDAO certifiedProductAsDao,
            CertifiedProductSearchResultDAO certifiedProductSearchResultDAO) {

        this.certificationResultService = certificationResultService;
        this.listingMeasureService = listingMeasureService;
        this.cqmResultsService = cqmResultsService;
        this.certificationStatusEventsService = certificationStatusEventsService;
        this.drService = drService;
        this.meaningfulUseUserHistoryService = meaningfulUseUserHistoryService;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.dimensionalDataManager = dimensionalDataManager;
        this.survManager = survManager;
        this.certifiedProductTestingLabDao = certifiedProductTestingLabDao;
        this.listingGraphDao = listingGraphDao;
        this.certifiedProductQmsStandardDao = certifiedProductQmsStandardDao;
        this.certifiedProductTargetedUserDao = certifiedProductTargetedUserDao;
        this.certifiedProductAsDao = certifiedProductAsDao;
        this.certifiedProductSearchResultDAO = certifiedProductSearchResultDAO;
    }

    public CertifiedProductSearchDetails createCertifiedSearchDetails(Long listingId) throws EntityRetrievalException {

        CertifiedProductDetailsDTO dto = certifiedProductSearchResultDAO.getById(listingId);
        CertifiedProductSearchDetails searchDetails = createCertifiedProductSearchDetailsBasic(certifiedProductSearchResultDAO.getById(listingId));

        searchDetails.setCertificationResults(certificationResultService.getCertificationResults(searchDetails));
        searchDetails.setCqmResults(cqmResultsService.getCqmResultDetails(dto.getId(), dto.getYear()));
        searchDetails.setCertificationEvents(certificationStatusEventsService.getCertificationStatusEvents(dto.getId()));
        searchDetails.setMeaningfulUseUserHistory(meaningfulUseUserHistoryService.getMeaningfulUseUserHistory(dto.getId()));
        searchDetails = populateDirectReviews(searchDetails);

        // get first-level parents and children
        searchDetails.getIcs().setParents(populateRelatedCertifiedProducts(getCertifiedProductParents(dto.getId())));
        searchDetails.getIcs().setChildren(populateRelatedCertifiedProducts(getCertifiedProductChildren(dto.getId())));
        return searchDetails;
    }

    public CertifiedProductSearchDetails createCertifiedProductSearchDetailsBasic(Long listingId) throws EntityRetrievalException {
        CertifiedProductDetailsDTO dto = certifiedProductSearchResultDAO.getById(listingId);
        return createCertifiedProductSearchDetailsBasic(dto);
    }

    public CertifiedProductSearchDetails createCertifiedProductSearchDetailsBasic(CertifiedProductDetailsDTO dto) throws EntityRetrievalException {
        CertifiedProductSearchDetails searchDetails = CertifiedProductSearchDetails.builder()
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
                .sedTestingEndDate(dto.getSedTestingEnd())
                .testingLabs(getTestingLabs(dto.getId()))
                .developer(new Developer(dto.getDeveloper()))
                .product(new Product(dto.getProduct()))
                .version(new ProductVersion(dto.getVersion()))
                .productAdditionalSoftware(dto.getProductAdditionalSoftware())
                .transparencyAttestationUrl(dto.getTransparencyAttestationUrl())
                .transparencyAttestation(dto.getTransparencyAttestation() != null ? new TransparencyAttestation(dto.getTransparencyAttestation()) : null)
                .lastModifiedDate(dto.getLastModifiedDate().getTime())
                .countCerts(dto.getCountCertifications())
                .countCqms(dto.getCountCqms())
                .countSurveillance(dto.getCountSurveillance())
                .countOpenSurveillance(dto.getCountOpenSurveillance())
                .countClosedSurveillance(dto.getCountClosedSurveillance())
                .countOpenNonconformities(dto.getCountOpenNonconformities())
                .countClosedNonconformities(dto.getCountClosedNonconformities())
                .surveillance(survManager.getByCertifiedProduct(dto.getId()))
                .qmsStandards(getCertifiedProductQmsStandards(dto.getId()))
                .measures(listingMeasureService.getCertifiedProductMeasures(dto.getId(), false))
                .targetedUsers(getCertifiedProductTargetedUsers(dto.getId()))
                .accessibilityStandards(getCertifiedProductAccessibilityStandards(dto.getId()))
                .rwtPlansUrl(dto.getRwtPlansUrl())
                .rwtPlansCheckDate(dto.getRwtPlansCheckDate())
                .rwtResultsUrl(dto.getRwtResultsUrl())
                .rwtResultsCheckDate(dto.getRwtResultsCheckDate())
                .rwtEligibilityYear(dto.getRwtEligibilityYear())
                .svapNoticeUrl(dto.getSvapNoticeUrl())
                .sed(new CertifiedProductSed())
                .testingLabs(getTestingLabs(dto.getId()))
                .build();

        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setInherits(dto.getIcs());
        searchDetails.setIcs(ics);
        return searchDetails;
    }

    private List<CertifiedProductTestingLab> getTestingLabs(Long listingId) throws EntityRetrievalException {
        return certifiedProductTestingLabDao.getTestingLabsByCertifiedProductId(listingId).stream()
                .map(dto -> new CertifiedProductTestingLab(dto))
                .collect(Collectors.toList());
    }

    private CertifiedProductSearchDetails populateDirectReviews(CertifiedProductSearchDetails listing) {
        List<DirectReview> drs = new ArrayList<DirectReview>();
        if (listing.getDeveloper() != null && listing.getDeveloper().getDeveloperId() != null) {
            drs = drService.getDirectReviewsRelatedToListing(listing.getId(),
                    listing.getDeveloper().getDeveloperId(),
                    MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY),
                    listing.getCertificationEvents());
        }
        listing.setDirectReviews(drs);
        listing.setDirectReviewsAvailable(drService.getDirectReviewsAvailable());
        return listing;
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
        CertificationStatusEventDTO cseDTO = certificationStatusEventsService.getInitialCertificationEvent(dto.getId());
        if (cseDTO != null) {
            cp.setCertificationDate(cseDTO.getEventDate().getTime());
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

    private List<CertifiedProductQmsStandard> getCertifiedProductQmsStandards(Long id) throws EntityRetrievalException {
        return certifiedProductQmsStandardDao.getQmsStandardsByCertifiedProductId(id).stream()
                .map(dto -> new CertifiedProductQmsStandard(dto))
                .collect(Collectors.toList());
    }

    private List<CertifiedProductTargetedUser> getCertifiedProductTargetedUsers(Long id) throws EntityRetrievalException {
        return certifiedProductTargetedUserDao.getTargetedUsersByCertifiedProductId(id).stream()
                .map(dto -> new CertifiedProductTargetedUser(dto))
                .collect(Collectors.toList());
    }

    private List<CertifiedProductAccessibilityStandard> getCertifiedProductAccessibilityStandards(Long id) throws EntityRetrievalException {
        return certifiedProductAsDao.getAccessibilityStandardsByCertifiedProductId(id).stream()
                .map(dto -> new CertifiedProductAccessibilityStandard(dto))
                .collect(Collectors.toList());
    }

}
