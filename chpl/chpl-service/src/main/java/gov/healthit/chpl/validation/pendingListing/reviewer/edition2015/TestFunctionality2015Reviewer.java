package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.TestFunctionalityCriteriaMapDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.pendingListing.reviewer.PermissionBasedReviewer;

/**
 * Confirms that the test functionality is valid for the criteria that is applying it.
 * @author kekey
 *
 */
@Component("pendingTestFunctionality2015Reviewer")
@DependsOn("certificationEditionDAO")
public class TestFunctionality2015Reviewer extends PermissionBasedReviewer {
    private TestFunctionalityDAO testFunctionalityDAO;
    private DimensionalDataManager dimensionalDataManager;

    @Autowired
    public TestFunctionality2015Reviewer(TestFunctionalityDAO testFunctionalityDAO,
            CertificationEditionDAO editionDAO,
            DimensionalDataManager dimensionalDataManager,
            ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.testFunctionalityDAO = testFunctionalityDAO;
        this.dimensionalDataManager = dimensionalDataManager;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        if (listing.getCertificationCriterion() != null) {
            for (PendingCertificationResultDTO cr : listing.getCertificationCriterion()) {
                if (cr.getMeetsCriteria() != null && cr.getMeetsCriteria().equals(Boolean.TRUE)
                        && cr.getTestFunctionality() != null) {
                    Iterator<PendingCertificationResultTestFunctionalityDTO> crtfIter =
                            cr.getTestFunctionality().iterator();
                    while (crtfIter.hasNext()) {
                        PendingCertificationResultTestFunctionalityDTO crtf = crtfIter.next();
                        TestFunctionalityDTO tf =
                                getTestFunctionality(crtf.getNumber(), getEdition(listing.getCertificationEdition()));
                        if (tf == null) {
                            addErrorOrWarningByPermission(listing, cr, "listing.criteria.testFunctionalityNotFoundAndRemoved",
                                    Util.formatCriteriaNumber(cr.getCriterion()), crtf.getNumber());
                            crtfIter.remove();
                        } else {
                            Set<String> warnings = getTestingFunctionalityWarningMessages(crtf, cr, listing);
                            if (warnings.size() > 0) {
                                listing.getWarningMessages().addAll(warnings);
                                //Remove the item
                                crtfIter.remove();
                            }
                        }
                    }
                }
            }
        }
    }

    private Set<String> getTestingFunctionalityWarningMessages(PendingCertificationResultTestFunctionalityDTO crtf,
            PendingCertificationResultDTO cr, PendingCertifiedProductDTO listing) {

        Set<String> warnings = new HashSet<String>();

        CertificationEdition edition = getEdition(getEditionFromListing(listing));
        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber(), edition.getCertificationEditionId());

        if (!isTestFunctionalityCritierionValid(cr.getCriterion().getId(), tf, edition.getYear())) {
            warnings.add(getTestFunctionalityCriterionMessage(crtf, cr, listing, edition));
        }
        return warnings;
    }

    private Boolean isTestFunctionalityCritierionValid(Long criteriaId, TestFunctionalityDTO tf, String year) {

        List<TestFunctionalityDTO> validTestFunctionalityForCriteria =
                testFunctionalityDAO.getTestFunctionalityCriteriaMaps(
                        CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()).get(criteriaId);

        if (validTestFunctionalityForCriteria == null) {
            return false;
        } else {
            //Is the TestFunctionalityDTO in the valid list (relies on the TestFunctionalityDTO.equals()
            return validTestFunctionalityForCriteria.contains(tf);
        }
    }

    private String getTestFunctionalityCriterionMessage(PendingCertificationResultTestFunctionalityDTO crtf,
            PendingCertificationResultDTO cr, PendingCertifiedProductDTO cp,
            CertificationEdition edition) {

        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber(), edition.getCertificationEditionId());
        return getMessage(
                Util.formatCriteriaNumber(cr.getCriterion()),
                crtf.getNumber(),
                getDelimitedListOfValidCriteriaNumbers(tf, edition),
                Util.formatCriteriaNumber(cr.getCriterion()));
    }

    private String getMessage(String criteriaNumber,
            String testFunctionalityNumber, String listOfValidCriteria, String currentCriterion) {

        return msgUtil.getMessage("listing.criteria.testFunctionalityCriterionMismatch",
                criteriaNumber, testFunctionalityNumber, listOfValidCriteria, currentCriterion);
    }

    private CertificationEdition getEdition(String year) {
        for (CertificationEdition edition : dimensionalDataManager.getCertificationEditions()) {
            if (edition.getYear().equals(year)) {
                return edition;
            }
        }
        return null;
    }

    private String getEditionFromListing(PendingCertifiedProductDTO listing) {
        return listing.getCertificationEdition();
    }

    private TestFunctionalityDTO getTestFunctionality(String number, Long editionId) {
        return testFunctionalityDAO.getByNumberAndEdition(number, editionId);
    }

    private String getDelimitedListOfValidCriteriaNumbers(TestFunctionalityDTO tfDTO,
            CertificationEdition edition) {

        StringBuilder criteria = new StringBuilder();
        List<CertificationCriterionDTO> certDTOs = new ArrayList<CertificationCriterionDTO>();

        List<TestFunctionalityCriteriaMapDTO> maps = testFunctionalityDAO.getTestFunctionalityCritieriaMaps();
        for (TestFunctionalityCriteriaMapDTO map : maps) {
            if (map.getCriteria().getCertificationEdition().equals(edition.getYear())) {
                if (tfDTO.getId().equals(map.getTestFunctionality().getId())) {
                    certDTOs.add(map.getCriteria());
                }
            }
        }

        Iterator<CertificationCriterionDTO> iter = certDTOs.iterator();
        while (iter.hasNext()) {
            criteria.append(Util.formatCriteriaNumber(iter.next()));
            if (iter.hasNext()) {
                criteria.append(", ");
            }
        }
        return criteria.toString();
    }

    private TestFunctionalityDTO getTestFunctionality(String number, CertificationEdition edition) {
        return testFunctionalityDAO.getByNumberAndEdition(number, edition.getCertificationEditionId());
    }
}
