package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.TestFunctionalityCriteriaMapDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.manager.TestingFunctionalityManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Confirms that the test functionality is valid for the criteria that is applying it.
 * @author kekey
 *
 */
@Component("pendingTestFunctionalityReviewer")
public class TestFunctionalityReviewer implements Reviewer, ApplicationListener<ContextRefreshedEvent> {
    private static final String EDITION_2014 = "2014";
    private static final String EDITION_2015 = "2015";

    private TestFunctionalityDAO testFunctionalityDAO;
    private TestingFunctionalityManager testFunctionalityManager;
    private ErrorMessageUtil msgUtil;
    private CertificationEditionDAO editionDAO;
    private List<CertificationEditionDTO> editionDTOs; 
    
    @Autowired
    public TestFunctionalityReviewer(TestFunctionalityDAO testFunctionalityDAO,
            TestingFunctionalityManager testFunctionalityManager, CertificationEditionDAO editionDAO,
            ErrorMessageUtil msgUtil) {
        this.testFunctionalityDAO = testFunctionalityDAO;
        this.testFunctionalityManager = testFunctionalityManager;
        this.editionDAO = editionDAO;
        this.msgUtil = msgUtil;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        editionDTOs = editionDAO.findAll();
    }

    @Override
    public void review(final PendingCertifiedProductDTO listing) {
        if (listing.getCertificationCriterion() != null) {
            for (PendingCertificationResultDTO cr : listing.getCertificationCriterion()) {
                if (cr.getTestFunctionality() != null) {
                    for (PendingCertificationResultTestFunctionalityDTO crtf : cr.getTestFunctionality()) {
                        listing.getErrorMessages().addAll(
                                getTestingFunctionalityErrorMessages(crtf, cr, listing));
                    }
                }
            }
        }
    }

    private Set<String> getTestingFunctionalityErrorMessages(final PendingCertificationResultTestFunctionalityDTO crtf,
            final PendingCertificationResultDTO cr, final PendingCertifiedProductDTO listing) {

        Set<String> errors = new HashSet<String>();

        CertificationEditionDTO edition = getEditionDTO(getEditionFromListing(listing));
        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber(), edition.getId());
        
        //Only validate practice type for 2014
        if (edition.getYear().equals(EDITION_2014)) {
            Long practiceTypeId = listing.getPracticeTypeId();
            if (!isTestFunctionalityPracticeTypeValid(practiceTypeId, tf)) {
                errors.add(getTestFunctionalityPracticeTypeErrorMessage(crtf, cr, listing));
            }
        }

        String criterionNumber = cr.getNumber();
        if (!isTestFunctionalityCritierionValid(criterionNumber, tf, edition.getYear())) {
            errors.add(getTestFunctionalityCriterionErrorMessage(crtf, cr, listing, edition));
        }
        return errors;
    }

    private Boolean isTestFunctionalityCritierionValid(final String criteriaNumber,
            final TestFunctionalityDTO tf, final String year) {

        List<TestFunctionalityDTO> validTestFunctionalityForCriteria = new ArrayList<TestFunctionalityDTO>();
        if (year.equals(EDITION_2014) &&
                testFunctionalityManager.getTestFunctionalityCriteriaMap2014().containsKey(criteriaNumber)) {
            validTestFunctionalityForCriteria = 
                    testFunctionalityManager.getTestFunctionalityCriteriaMap2014().get(criteriaNumber);
        } else if (year.equals(EDITION_2015) &&
                testFunctionalityManager.getTestFunctionalityCriteriaMap2015().containsKey(criteriaNumber)) {
            validTestFunctionalityForCriteria = 
                    testFunctionalityManager.getTestFunctionalityCriteriaMap2015().get(criteriaNumber);
        }
        
        //Is the TestFunctionalityDTO in the valid list (relies on the TestFunctionalityDTO.equals()
        return validTestFunctionalityForCriteria.contains(tf);
}

    private Boolean isTestFunctionalityPracticeTypeValid(final Long practiceTypeId,
            final TestFunctionalityDTO tf) {

        if (tf.getPracticeType() != null) {
            if (!practiceTypeId.equals(tf.getPracticeType().getId())) {
                return false;
            }
        }
        return true;
    }

    private String getTestFunctionalityPracticeTypeErrorMessage(final PendingCertificationResultTestFunctionalityDTO crtf,
            final PendingCertificationResultDTO cr, final PendingCertifiedProductDTO cp) {
        CertificationEditionDTO editionDTO = getEditionDTO(getEditionFromListing(cp));
        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber(), editionDTO.getId());

        return getTestFunctionalityPracticeTypeErrorMessage(
                cr.getNumber(),
                crtf.getNumber(),
                tf.getPracticeType().getName(),
                cp.getPracticeType());
    }

    private String getTestFunctionalityPracticeTypeErrorMessage(final String criteriaNumber,
            final String testFunctionalityNumber, final String validPracticeTypeName,
            final String currentPracticeTypeName) {

        return msgUtil.getMessage("listing.criteria.testFunctionalityPracticeTypeMismatch",
                criteriaNumber, testFunctionalityNumber, validPracticeTypeName, currentPracticeTypeName);
    }

    private String getTestFunctionalityCriterionErrorMessage(final PendingCertificationResultTestFunctionalityDTO crtf,
            final PendingCertificationResultDTO cr, final PendingCertifiedProductDTO cp, 
            final CertificationEditionDTO edition) {

        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber(), edition.getId());
        return getTestFunctionalityCriterionErrorMessage(
                cr.getNumber(),
                crtf.getNumber(),
                getDelimitedListOfValidCriteriaNumbers(tf, edition),
                cr.getNumber());
    }

    private String getTestFunctionalityCriterionErrorMessage(final String criteriaNumber,
            final String testFunctionalityNumber, final String listOfValidCriteria, final String currentCriterion) {

        return msgUtil.getMessage("listing.criteria.testFunctionalityCriterionMismatch",
                criteriaNumber, testFunctionalityNumber, listOfValidCriteria, currentCriterion);
    }
    
    private CertificationEditionDTO getEditionDTO(final String year) {
        for (CertificationEditionDTO dto : editionDTOs) {
            if (dto.getYear().equals(year)) {
                return dto;
            }
        }
        return null;
    }
    
    private String getEditionFromListing(final PendingCertifiedProductDTO listing) {
        return listing.getCertificationEdition();
    }
    
    private TestFunctionalityDTO getTestFunctionality(final String number, final Long editionId) {
        return testFunctionalityDAO.getByNumberAndEdition(number, editionId);
    }

    private String getDelimitedListOfValidCriteriaNumbers(final TestFunctionalityDTO tfDTO,
            final CertificationEditionDTO edition) {

        String criteria = "";
        List<CertificationCriterionDTO> certDTOs = new ArrayList<CertificationCriterionDTO>();
        
        List<TestFunctionalityCriteriaMapDTO> maps = testFunctionalityDAO.getTestFunctionalityCritieriaMaps();
        for (TestFunctionalityCriteriaMapDTO map : maps) {
            if (map.getCriteria().getCertificationEdition().equals(edition.getYear())) {
                if (tfDTO.getId().equals(map.getTestFunctionality().getId()))
                    certDTOs.add(map.getCriteria());
            }
        }
        
        Iterator<CertificationCriterionDTO> iter = certDTOs.iterator();
        while(iter.hasNext()) {
            criteria += iter.next().getNumber();
            if (iter.hasNext()) {
                criteria += ", ";
            }
        }
        return criteria;
    }
}
