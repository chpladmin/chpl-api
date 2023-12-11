package gov.healthit.chpl.certifiedproduct.service;

import static gov.healthit.chpl.util.LambdaExceptionUtil.rethrowConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CertificationResultSynchronizationService {
    private CertificationResultManager certResultManager;
    private CertificationResultDAO certResultDao;

    @Autowired
    public CertificationResultSynchronizationService(CertificationResultManager certResultManager,
            CertificationResultDAO certResultDao) {
        this.certResultManager = certResultManager;
        this.certResultDao = certResultDao;
    }

    public int synchronizeCertificationResults(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing, List<CertificationResult> origCertResults,
            List<CertificationResult> newCertResults) throws EntityCreationException, EntityRetrievalException, Exception {

        List<CertificationResult> updatedCertResults = new ArrayList<CertificationResult>();
        List<CertificationResult> addedCertResults = new ArrayList<CertificationResult>();
        List<CertificationResult> removedCertResults = new ArrayList<CertificationResult>();

        //Find the updated certification results
        if (!CollectionUtils.isEmpty(newCertResults)) {
            updatedCertResults = newCertResults.stream()
                    .filter(cr -> {
                        Optional<CertificationResult> found = getMatchingItemInList(cr, origCertResults);
                        return found.isPresent();
                    })
                    .toList();

            updatedCertResults.forEach(rethrowConsumer(updatedCr ->
                certResultManager.createOrUpdate(existingListing, updatedListing,
                        getMatchingItemInList(updatedCr, origCertResults).get(),
                        updatedCr)));
        }

        //Find the added certification results
        if (!CollectionUtils.isEmpty(newCertResults)) {
            addedCertResults = newCertResults.stream()
                    .filter(cr -> getMatchingItemInList(cr, origCertResults).isEmpty())
                    .toList();

            addedCertResults.forEach(rethrowConsumer(addedCertResult ->
                certResultManager.createOrUpdate(existingListing, updatedListing,
                        CertificationResult.builder()
                            .criterion(addedCertResult.getCriterion())
                            .success(false)
                            .build(),
                        addedCertResult)));
        }

        //Find the removed
        if (!CollectionUtils.isEmpty(origCertResults)) {
            removedCertResults = origCertResults.stream()
                    .filter(cr -> getMatchingItemInList(cr, newCertResults).isEmpty())
                    .toList();

            removedCertResults.forEach(x -> LOGGER.info("Removed Certification Result: {}", Util.formatCriteriaNumber(x.getCriterion())));

            removedCertResults.forEach(removedCertResult -> certResultDao.delete(
                    getMatchingItemInList(removedCertResult, origCertResults).get().getId()));
        }

        return updatedCertResults.size() + addedCertResults.size() + removedCertResults.size();
    }

    private Optional<CertificationResult> getMatchingItemInList(CertificationResult cr, List<CertificationResult> certificationResults) {
        if (CollectionUtils.isEmpty(certificationResults)) {
            return Optional.empty();
        }
        return certificationResults.stream()
                .filter(certificationResult ->
                certificationResult != null && certificationResult.getCriterion() != null
                            ? certificationResult.getCriterion().getId().equals(cr.getCriterion().getId())
                            : false)
                .findAny();
    }
}
