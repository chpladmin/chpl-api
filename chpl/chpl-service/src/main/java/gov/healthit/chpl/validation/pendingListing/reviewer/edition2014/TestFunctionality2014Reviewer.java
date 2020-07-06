package gov.healthit.chpl.validation.pendingListing.reviewer.edition2014;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.TestFunctionalityCriteriaMapDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.manager.TestingFunctionalityManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

/**
 * Confirms that the test functionality is valid for the criteria that is applying it.
 * @author kekey
 *
 */
@Component("pendingTestFunctionality2014Reviewer")
public class TestFunctionality2014Reviewer implements Reviewer {
    private TestFunctionalityDAO testFunctionalityDAO;
    private TestingFunctionalityManager testFunctionalityManager;
    private DimensionalDataManager dimensionalDataManager;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestFunctionality2014Reviewer(TestFunctionalityDAO testFunctionalityDAO,
            TestingFunctionalityManager testFunctionalityManager, CertificationEditionDAO editionDAO,
            DimensionalDataManager dimensionalDataManager, ErrorMessageUtil msgUtil) {
        this.testFunctionalityDAO = testFunctionalityDAO;
        this.testFunctionalityManager = testFunctionalityManager;
        this.dimensionalDataManager = dimensionalDataManager;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        if (listing.getCertificationCriterion() != null) {
            for (PendingCertificationResultDTO cr : listing.getCertificationCriterion()) {
                if (cr.getTestFunctionality() != null) {
                    Iterator<PendingCertificationResultTestFunctionalityDTO> crtfIter =
                            cr.getTestFunctionality().iterator();
                    while (crtfIter.hasNext()) {
                        PendingCertificationResultTestFunctionalityDTO crtf = crtfIter.next();
                        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber());
                        if (tf == null) {
                            listing.getErrorMessages().add(
                                    msgUtil.getMessage("listing.criteria.testFunctionalityNotFoundAndRemoved",
                                            cr.getCriterion().getNumber(), crtf.getNumber()));
                            crtfIter.remove();
                        } else {
                            listing.getErrorMessages().addAll(
                                    getTestingFunctionalityErrorMessages(crtf, cr, listing));

                            Set<String> warnings = getTestingFunctionalityWarningMessages(crtf, cr, listing);
                            if (warnings.size() > 0) {
                                listing.getWarningMessages().addAll(warnings);
                                crtfIter.remove();
                            }
                        }
                    }
                }
            }
        }
    }

    private Set<String> getTestingFunctionalityErrorMessages(PendingCertificationResultTestFunctionalityDTO crtf,
            PendingCertificationResultDTO cr, PendingCertifiedProductDTO listing) {

        Set<String> errors = new HashSet<String>();

        CertificationEdition edition = getEdition(getEditionFromListing(listing));
        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber(), edition.getCertificationEditionId());

        Long practiceTypeId = listing.getPracticeTypeId();
        if (!isTestFunctionalityPracticeTypeValid(practiceTypeId, tf)) {
            errors.add(getTestFunctionalityPracticeTypeErrorMessage(crtf, cr, listing));
        }
        return errors;
    }

    private Set<String> getTestingFunctionalityWarningMessages(PendingCertificationResultTestFunctionalityDTO crtf,
            PendingCertificationResultDTO cr, PendingCertifiedProductDTO listing) {

        Set<String> warnings = new HashSet<String>();
        CertificationEdition edition = getEdition(getEditionFromListing(listing));
        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber(), edition.getCertificationEditionId());

        if (!isTestFunctionalityCritierionValid(cr.getCriterion().getId(), tf, edition.getYear())) {
            warnings.add(getTestFunctionalityCriterionErrorMessage(crtf, cr, listing, edition));
        }
        return warnings;
    }

    private Boolean isTestFunctionalityCritierionValid(Long criteriaId, TestFunctionalityDTO tf, String year) {

        List<TestFunctionalityDTO> validTestFunctionalityForCriteria =
                testFunctionalityManager.getTestFunctionalityCriteriaMap2014().get(criteriaId);

        //Is the TestFunctionalityDTO in the valid list (relies on the TestFunctionalityDTO.equals()
        return validTestFunctionalityForCriteria.contains(tf);
    }

    private Boolean isTestFunctionalityPracticeTypeValid(Long practiceTypeId,
            TestFunctionalityDTO tf) {

        if (tf.getPracticeType() != null) {
            if (!practiceTypeId.equals(tf.getPracticeType().getId())) {
                return false;
            }
        }
        return true;
    }

    private String getTestFunctionalityPracticeTypeErrorMessage(PendingCertificationResultTestFunctionalityDTO crtf,
            PendingCertificationResultDTO cr, PendingCertifiedProductDTO cp) {
        CertificationEdition edition = getEdition(getEditionFromListing(cp));
        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber(), edition.getCertificationEditionId());

        return getTestFunctionalityPracticeTypeErrorMessage(
                cr.getCriterion().getNumber(),
                crtf.getNumber(),
                tf.getPracticeType().getName(),
                cp.getPracticeType());
    }

    private String getTestFunctionalityPracticeTypeErrorMessage(String criteriaNumber,
            String testFunctionalityNumber, String validPracticeTypeName,
            String currentPracticeTypeName) {

        return msgUtil.getMessage("listing.criteria.testFunctionalityPracticeTypeMismatch",
                criteriaNumber, testFunctionalityNumber, validPracticeTypeName, currentPracticeTypeName);
    }

    private String getTestFunctionalityCriterionErrorMessage(PendingCertificationResultTestFunctionalityDTO crtf,
            PendingCertificationResultDTO cr, PendingCertifiedProductDTO cp,
            CertificationEdition edition) {

        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber(), edition.getCertificationEditionId());
        return getTestFunctionalityCriterionErrorMessage(
                cr.getCriterion().getNumber(),
                crtf.getNumber(),
                getDelimitedListOfValidCriteriaNumbers(tf, edition),
                cr.getCriterion().getNumber());
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
            criteria.append(iter.next().getNumber());
            if (iter.hasNext()) {
                criteria.append(", ");
            }
        }
        return criteria.toString();
    }

    private TestFunctionalityDTO getTestFunctionality(String number) {
        Long editionId = 2L;
        return testFunctionalityDAO.getByNumberAndEdition(number, editionId);
    }
}
