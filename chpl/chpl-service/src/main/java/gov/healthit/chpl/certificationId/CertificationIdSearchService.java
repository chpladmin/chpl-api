package gov.healthit.chpl.certificationId;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.CertificationIdException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.CertifiedProductManager;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CertificationIdSearchService {
    private static final String DEFAULT_YEAR = "2015";

    private CertificationIdManager certificationIdManager;
    private CertifiedProductManager certifiedProductManager;
    private CertificationIdYearCalculator certIdYearCalculator;
    private ValidatorFactory validatorFactory;

    @Autowired
    public CertificationIdSearchService(CertificationIdManager certificationIdManager,
            CertifiedProductManager certifiedProductManager,
            CertificationIdYearCalculator certIdYearCalculator,
            ValidatorFactory validatorFactory) {
        this.certificationIdManager = certificationIdManager;
        this.certifiedProductManager = certifiedProductManager;
        this.certIdYearCalculator = certIdYearCalculator;
        this.validatorFactory = validatorFactory;
    }

    @Transactional
    public CertificationIdLookupResults findCertificationIdByCertificationId(String certificationId,
            Boolean includeCriteria,
            Boolean includeCqms) throws InvalidArgumentsException, EntityRetrievalException, CertificationIdException {
        CertificationIdLookupResults results = new CertificationIdLookupResults();
        try {
            // Lookup the Cert ID
            CertificationIdDTO certId = certificationIdManager.getByCertificationId(certificationId);
            if (certId != null) {
                results.setEhrCertificationId(certId.getCertificationId());
                results.setYear(certId.getYear());

                // Find the listings associated with the Cert ID
                List<Long> listingIds = certificationIdManager.getListingIdsByCertificationId(certId.getId());
                List<CertifiedProductDetailsDTO> listingDtos = certifiedProductManager.getDetailsByIds(listingIds);

                SortedSet<Integer> yearSet = new TreeSet<Integer>();
                List<Long> certProductIds = new ArrayList<Long>();

                // Add product data to results
                List<CertificationIdLookupResults.Product> productList = results.getProducts();
                for (CertifiedProductDetailsDTO listingDto : listingDtos) {
                    if (StringUtils.isEmpty(listingDto.getYear())) {
                        listingDto.setYear(DEFAULT_YEAR);
                    }
                    productList.add(new CertificationIdLookupResults.Product(listingDto));
                    yearSet.add(Integer.valueOf(listingDto.getYear()));
                    certProductIds.add(listingDto.getId());
                }

                // Add criteria and cqms met to results
                if (includeCriteria || includeCqms) {
                    Validator validator = this.validatorFactory.getValidator(certId.getYear());

                    // Lookup Criteria for Validating
                    List<CertificationCriterion> criteria = certificationIdManager
                            .getCriteriaMetByCertifiedProductIds(certProductIds);

                    // Lookup CQMs for Validating
                    List<CQMMetDTO> cqmDtos = certificationIdManager.getCqmsMetByCertifiedProductIds(certProductIds);

                    boolean isValid = validator.validate(criteria, cqmDtos, new ArrayList<Integer>(yearSet));
                    if (isValid) {
                        if (includeCriteria) {
                            results.setCriteria(validator.getCriteriaMet().keySet());
                        }
                        if (includeCqms) {
                            results.setCqms(validator.getCqmsMet().keySet());
                        }
                    }
                }

            }
        } catch (final EntityRetrievalException ex) {
            throw new EntityRetrievalException("Unable to lookup Certification ID " + certificationId + ".");
        }

        return results;
    }

    public CertificationIdResults findCertificationByProductIds(List<Long> listingIds, Boolean create)
            throws InvalidArgumentsException, CertificationIdException {
        if (listingIds == null) {
            listingIds = new ArrayList<Long>();
        }

        List<CertifiedProductDetailsDTO> listingDtos = new ArrayList<CertifiedProductDetailsDTO>();
        try {
            listingDtos = certifiedProductManager.getDetailsByIds(listingIds);
        } catch (EntityRetrievalException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        // Add products to results
        CertificationIdResults results = new CertificationIdResults();
        SortedSet<Integer> yearSet = new TreeSet<Integer>();
        List<CertificationIdResults.Product> resultProducts = new ArrayList<CertificationIdResults.Product>();
        for (CertifiedProductDetailsDTO listingDto : listingDtos) {
            if (create) {
                if (!isEditionlessOrCuresUpdate(listingDto)) {
                    throw new InvalidArgumentsException("New Certification IDs can only be created using 2015 Cures Update Listings");
                }
            }

            if (StringUtils.isEmpty(listingDto.getYear())) {
                listingDto.setYear(DEFAULT_YEAR);
            }
            CertificationIdResults.Product p = new CertificationIdResults.Product(listingDto);
            resultProducts.add(p);
            yearSet.add(Integer.valueOf(certIdYearCalculator.getCurrentCertIdYear(listingDto.getYear())));
        }
        results.setProducts(resultProducts);
        String year = Validator.calculateAttestationYear(yearSet);
        results.setYear(year);

        // Validate the collection
        Validator validator = this.validatorFactory.getValidator(year);

        // Lookup Criteria for Validating
        List<CertificationCriterion> criteria = certificationIdManager.getCriteriaMetByCertifiedProductIds(listingIds);

        // Lookup CQMs for Validating
        List<CQMMetDTO> cqmDtos = certificationIdManager.getCqmsMetByCertifiedProductIds(listingIds);

        boolean isValid = validator.validate(criteria, cqmDtos, new ArrayList<Integer>(yearSet));
        results.setValid(isValid);
        results.setMetPercentages(validator.getPercents());
        results.setMetCounts(validator.getCounts());
        results.setMissingCombo(validator.getMissingCombo());
        results.setMissingOr(validator.getMissingOr());
        results.setMissingAnd(validator.getMissingAnd());
        results.setMissingXOr(validator.getMissingXOr());

        // Lookup CERT ID
        if (validator.isValid()) {
            CertificationIdDTO idDto = null;
            try {
                idDto = certificationIdManager.getByListings(listingDtos, year);
                if (null != idDto) {
                    results.setEhrCertificationId(idDto.getCertificationId());
                } else {
                    if ((create) && (results.isValid())) {
                        // Generate a new ID
                        idDto = certificationIdManager.create(listingIds, year);
                        results.setEhrCertificationId(idDto.getCertificationId());
                    }
                }
            } catch (EntityRetrievalException | EntityCreationException | ActivityException ex) {
                throw new CertificationIdException("Unable to retrieve a Certification ID.");
            }
        }
        return results;
    }

    private boolean isEditionlessOrCuresUpdate(CertifiedProductDetailsDTO listing) {
        if (StringUtils.isEmpty(listing.getYear()) && listing.getCuresUpdate() == null) {
            return true;
        }
        return listing.getYear().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())
                && BooleanUtils.isTrue(listing.getCuresUpdate());
    }
}
