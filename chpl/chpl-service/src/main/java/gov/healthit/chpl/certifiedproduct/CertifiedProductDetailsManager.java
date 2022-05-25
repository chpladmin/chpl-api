package gov.healthit.chpl.certifiedproduct;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certifiedproduct.service.CertificationResultService;
import gov.healthit.chpl.certifiedproduct.service.CertificationStatusEventsService;
import gov.healthit.chpl.certifiedproduct.service.CqmResultsService;
import gov.healthit.chpl.certifiedproduct.service.ListingMeasuresService;
import gov.healthit.chpl.certifiedproduct.service.ListingService;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.sharedstore.SharedListingStoreProvider;
import lombok.extern.log4j.Log4j2;

@Component("certifiedProductDetailsManager")
@Log4j2
public class CertifiedProductDetailsManager {
    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    private ListingService listingService;
    private CqmResultsService cqmResultsService;
    private CertificationResultService certificationResultService;
    private ListingMeasuresService listingMeasuresService;
    private CertificationStatusEventsService certificationStatusEventsService;
    private SharedListingStoreProvider sharedListingStoreProvider;

    @Autowired
    public CertifiedProductDetailsManager(
            CertifiedProductSearchResultDAO certifiedProductSearchResultDAO,
            ListingService listingService,
            CqmResultsService cqmResultsService,
            CertificationResultService certificationResultService,
            ListingMeasuresService listingMeasuresService,
            CertificationStatusEventsService certificationStatusEventsService,
            SharedListingStoreProvider sharedListingStoreProvider) {

        this.certifiedProductSearchResultDAO = certifiedProductSearchResultDAO;
        this.listingService = listingService;
        this.cqmResultsService = cqmResultsService;
        this.certificationResultService = certificationResultService;
        this.listingMeasuresService = listingMeasuresService;
        this.certificationStatusEventsService = certificationStatusEventsService;
        this.sharedListingStoreProvider = sharedListingStoreProvider;
    }

    @Transactional(readOnly = true)
    public CertifiedProductSearchDetails getCertifiedProductDetailsByChplProductNumber(String chplProductNumber) throws EntityRetrievalException {
        CertifiedProductDetailsDTO dto = getCertifiedProductDetailsDtoByChplProductNumber(chplProductNumber);
        return listingService.createCertifiedSearchDetails(dto.getId());
    }

    //@Transactional(readOnly = true)
    //public CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId) throws EntityRetrievalException {
    //    return listingService.createCertifiedSearchDetails(certifiedProductId);
    //}

    @Transactional(readOnly = true)
    public CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId) throws EntityRetrievalException {
        return sharedListingStoreProvider.get(certifiedProductId, () -> {
            try {
                return listingService.createCertifiedSearchDetails(certifiedProductId);
            } catch (EntityRetrievalException e) {
                LOGGER.error(e);
                return null;
            }
        });
    }

    @Transactional(readOnly = true)
    public CertifiedProductSearchDetails getCertifiedProductDetailsBasicByChplProductNumber(String chplProductNumber) throws EntityRetrievalException {
        CertifiedProductDetailsDTO dto = getCertifiedProductDetailsDtoByChplProductNumber(chplProductNumber);
        return listingService.createCertifiedProductSearchDetailsBasic(dto.getId());
    }

    @Transactional(readOnly = true)
    public CertifiedProductSearchDetails getCertifiedProductDetailsBasic(Long certifiedProductId) throws EntityRetrievalException {
        return listingService.createCertifiedProductSearchDetailsBasic(certifiedProductId);
    }

    @Transactional(readOnly = true)
    public List<CQMResultDetails> getCertifiedProductCqms(Long certifiedProductId) throws EntityRetrievalException {
        CertifiedProductDetailsDTO dto = certifiedProductSearchResultDAO.getById(certifiedProductId);
        return cqmResultsService.getCqmResultDetails(certifiedProductId, dto.getYear());
    }

    @Transactional(readOnly = true)
    public List<CQMResultDetails> getCertifiedProductCqms(String chplProductNumber) throws EntityRetrievalException {
        CertifiedProductDetailsDTO dto = getCertifiedProductDetailsDtoByChplProductNumber(chplProductNumber);
        return cqmResultsService.getCqmResultDetails(dto.getId(), dto.getYear());
    }

    @Transactional(readOnly = true)
    public List<CertificationResult> getCertifiedProductCertificationResults(Long certifiedProductId) throws EntityRetrievalException {
        CertifiedProductDetailsDTO dto = certifiedProductSearchResultDAO.getById(certifiedProductId);
        CertifiedProductSearchDetails searchDetails = listingService.createCertifiedProductSearchDetailsWithBasicDataOnly(dto);
        return certificationResultService.getCertificationResults(searchDetails);
    }

    @Transactional(readOnly = true)
    public List<CertificationResult> getCertifiedProductCertificationResults(String chplProductNumber) throws EntityRetrievalException {
        CertifiedProductDetailsDTO dto = getCertifiedProductDetailsDtoByChplProductNumber(chplProductNumber);
        CertifiedProductSearchDetails searchDetails = listingService.createCertifiedProductSearchDetailsWithBasicDataOnly(dto);
        return certificationResultService.getCertificationResults(searchDetails);
    }

    @Transactional(readOnly = true)
    public List<ListingMeasure> getCertifiedProductMeasures(Long listingId) throws EntityRetrievalException {
        return getCertifiedProductMeasures(listingId, false);
    }

    @Transactional(readOnly = true)
    public List<ListingMeasure> getCertifiedProductMeasures(Long listingId, Boolean checkIfListingExists) throws EntityRetrievalException {
        return listingMeasuresService.getCertifiedProductMeasures(listingId, checkIfListingExists);
    }

    @Transactional(readOnly = true)
    public List<ListingMeasure> getCertifiedProductMeasures(String chplProductNumber) throws EntityRetrievalException {
        CertifiedProductDetailsDTO dto = getCertifiedProductDetailsDtoByChplProductNumber(chplProductNumber);
        return getCertifiedProductMeasures(dto.getId());
    }

    @Transactional(readOnly = true)
    public List<CertificationStatusEvent> getCertificationStatusEvents(Long listingId) throws EntityRetrievalException {
        return certificationStatusEventsService.getCertificationStatusEvents(listingId);
    }

    private CertifiedProductDetailsDTO getCertifiedProductDetailsDtoByChplProductNumber(String chplProductNumber) throws EntityRetrievalException {
        List<CertifiedProductDetailsDTO> dtos = certifiedProductSearchResultDAO.getByChplProductNumber(chplProductNumber);
        if (dtos.size() == 0) {
            throw new EntityRetrievalException("Could not retrieve CertifiedProductSearchDetails.");
        }
        return dtos.get(0);
    }
}
