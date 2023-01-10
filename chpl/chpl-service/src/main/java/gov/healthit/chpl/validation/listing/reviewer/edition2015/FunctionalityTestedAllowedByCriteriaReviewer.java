package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedCriteriaMap;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedDAO;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

@Component("functionalityTestedAllowedByCriteriaReviewer")
@Transactional
@DependsOn("certificationEditionDAO")
public class FunctionalityTestedAllowedByCriteriaReviewer extends PermissionBasedReviewer {
    private FunctionalityTestedDAO functionalityTestedDao;
    private DimensionalDataManager dimensionalDataManager;

    @Autowired
    public FunctionalityTestedAllowedByCriteriaReviewer(FunctionalityTestedDAO functionalityTestedDao,
            CertificationEditionDAO editionDAO,
            DimensionalDataManager dimensionalDataManager,
            ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.functionalityTestedDao = functionalityTestedDao;
        this.dimensionalDataManager = dimensionalDataManager;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null) {
            for (CertificationResult cr : listing.getCertificationResults()) {
                if (BooleanUtils.isTrue(cr.isSuccess()) && cr.getFunctionalitiesTested() != null) {
                    for (CertificationResultFunctionalityTested crft : cr.getFunctionalitiesTested()) {
                        Set<String> messages = getFunctionalitiesTestedErrorMessages(crft, cr, listing);
                        for (String message : messages) {
                            addCriterionError(listing, cr, message);
                        }
                    }
                }
            }
        }
    }

    private Set<String> getFunctionalitiesTestedErrorMessages(CertificationResultFunctionalityTested crft,
            CertificationResult cr, CertifiedProductSearchDetails listing) {

        Set<String> errors = new HashSet<String>();

        CertificationEdition edition = getEdition(getEditionFromListing(listing));
        FunctionalityTested functionalityTested = null;
        if (crft.getFunctionalityTestedId() != null) {
            functionalityTested = getFunctionalityTested(crft.getFunctionalityTestedId(), edition.getCertificationEditionId());
            if (functionalityTested == null) {
                errors.add(msgUtil.getMessage("listing.criteria.invalidTestFunctionalityId", Util.formatCriteriaNumber(cr.getCriterion()), crft.getFunctionalityTestedId()));
            }
        } else if (!StringUtils.isEmpty(crft.getName())) {
            functionalityTested = getFunctionalityTested(crft.getName(), edition.getCertificationEditionId());
            if (!isFunctionalityTestedCritierionValid(cr.getCriterion().getId(), functionalityTested, edition.getYear())) {
                errors.add(getFunctionalitiesTestedCriterionErrorMessage(crft, cr, listing, edition));
            }
        }
        return errors;
    }

    private Boolean isFunctionalityTestedCritierionValid(Long criteriaId, FunctionalityTested functionalityTested, String year) {
        List<FunctionalityTested> validFunctionalityTestedForCriteria =
                functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps(year).get(criteriaId);

        if (validFunctionalityTestedForCriteria == null) {
            return false;
        } else {
            //Is the functionality tested in the valid list (relies on the FunctionalityTested.equals()
            return validFunctionalityTestedForCriteria.contains(functionalityTested);
        }
    }

    private String getFunctionalitiesTestedCriterionErrorMessage(CertificationResultFunctionalityTested crft,
            CertificationResult cr, CertifiedProductSearchDetails cp,
            CertificationEdition edition) {

        FunctionalityTested functionalityTested = getFunctionalityTested(crft.getFunctionalityTestedId(), edition.getCertificationEditionId());
        if (functionalityTested == null || functionalityTested.getId() == null) {
            return msgUtil.getMessage("listing.criteria.invalidTestFunctionality", Util.formatCriteriaNumber(cr.getCriterion()), crft.getName());
        }
        return getFunctionalityTestedCriterionErrorMessage(
                Util.formatCriteriaNumber(cr.getCriterion()),
                crft.getName(),
                getDelimitedListOfValidCriteriaNumbers(functionalityTested, edition),
                Util.formatCriteriaNumber(cr.getCriterion()));
    }

    private String getFunctionalityTestedCriterionErrorMessage(String criteriaNumber,
            String functionalityTestedNumber, String listOfValidCriteria, String currentCriterion) {

        return msgUtil.getMessage("listing.criteria.testFunctionalityCriterionMismatch",
                criteriaNumber, functionalityTestedNumber, listOfValidCriteria, currentCriterion);
    }

    private CertificationEdition getEdition(String year) {
        for (CertificationEdition edition : dimensionalDataManager.getCertificationEditions()) {
            if (edition.getYear().equals(year)) {
                return edition;
            }
        }
        return null;
    }

    private String getEditionFromListing(CertifiedProductSearchDetails listing) {
        String edition = "";
        if (listing.getCertificationEdition().containsKey(CertifiedProductSearchDetails.EDITION_NAME_KEY)) {
            edition = listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString();
        }
        return edition;
    }

    private FunctionalityTested getFunctionalityTested(Long functionalityTestedId, Long editionId) {
        return functionalityTestedDao.getByIdAndEdition(functionalityTestedId, editionId);
    }

    private FunctionalityTested getFunctionalityTested(String functionalityTestedNumber, Long editionId) {
        return functionalityTestedDao.getByNumberAndEdition(functionalityTestedNumber, editionId);
    }

    private String getDelimitedListOfValidCriteriaNumbers(FunctionalityTested functionalityTested,
            CertificationEdition edition) {

        StringBuilder criteriaNumbers = new StringBuilder();
        List<CertificationCriterion> criteria = new ArrayList<CertificationCriterion>();

        List<FunctionalityTestedCriteriaMap> maps = functionalityTestedDao.getFunctionalitiesTestedCritieriaMaps();
        for (FunctionalityTestedCriteriaMap map : maps) {
            if (map.getCriterion().getCertificationEdition().equals(edition.getYear())) {
                if (functionalityTested.getId().equals(map.getFunctionalityTested().getId())) {
                    criteria.add(map.getCriterion());
                }
            }
        }

        Iterator<CertificationCriterion> iter = criteria.iterator();
        while (iter.hasNext()) {
            criteriaNumbers.append(Util.formatCriteriaNumber(iter.next()));
            if (iter.hasNext()) {
                criteriaNumbers.append(", ");
            }
        }
        return criteriaNumbers.toString();
    }
}
