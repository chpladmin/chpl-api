package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.TestFunctionalityCriteriaMapDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

/**
 * Makes sure test functionality is valid given criteria it is applied to.
 * @author tyoung
 *
 */
@Component("testFunctionality2015Reviewer")
@Transactional
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
    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null) {
            for (CertificationResult cr : listing.getCertificationResults()) {
                if (cr.isSuccess() != null && cr.isSuccess().equals(Boolean.TRUE)
                        && cr.getTestFunctionality() != null) {
                    for (CertificationResultTestFunctionality crtf : cr.getTestFunctionality()) {
                        Set<String> messages = getTestingFunctionalityErrorMessages(crtf, cr, listing);
                        for (String message : messages) {
                            addCriterionErrorOrWarningByPermission(listing, cr, message);
                        }
                    }
                }
            }
        }
    }

    private Set<String> getTestingFunctionalityErrorMessages(CertificationResultTestFunctionality crtf,
            CertificationResult cr, CertifiedProductSearchDetails listing) {

        Set<String> errors = new HashSet<String>();

        CertificationEdition edition = getEdition(getEditionFromListing(listing));
        TestFunctionalityDTO tf = getTestFunctionality(crtf.getName(), edition.getCertificationEditionId());

        if (!isTestFunctionalityCritierionValid(cr.getCriterion().getId(), tf, edition.getYear())) {
            errors.add(getTestFunctionalityCriterionErrorMessage(crtf, cr, listing, edition));
        }
        return errors;
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

    private String getTestFunctionalityCriterionErrorMessage(CertificationResultTestFunctionality crtf,
            CertificationResult cr, CertifiedProductSearchDetails cp,
            CertificationEdition edition) {

        TestFunctionalityDTO tf = getTestFunctionality(crtf.getName(), edition.getCertificationEditionId());
        return getTestFunctionalityCriterionErrorMessage(
                Util.formatCriteriaNumber(cr.getCriterion()),
                crtf.getName(),
                getDelimitedListOfValidCriteriaNumbers(tf, edition),
                Util.formatCriteriaNumber(cr.getCriterion()));
    }

    private String getTestFunctionalityCriterionErrorMessage(String criteriaNumber,
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

    private String getEditionFromListing(CertifiedProductSearchDetails listing) {
        String edition = "";
        if (listing.getCertificationEdition().containsKey(CertifiedProductSearchDetails.EDITION_NAME_KEY)) {
            edition = listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString();
        }
        return edition;
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
}
