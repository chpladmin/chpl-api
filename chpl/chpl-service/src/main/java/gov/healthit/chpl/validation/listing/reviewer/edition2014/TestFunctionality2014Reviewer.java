package gov.healthit.chpl.validation.listing.reviewer.edition2014;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.TestFunctionalityCriteriaMapDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.manager.TestingFunctionalityManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

/**
 * Makes sure test functionality is valid given the practice type
 * and criteria it is applied to.
 * @author kekey
 *
 */
@Component("testFunctionality2014Reviewer")
@Transactional
public class TestFunctionality2014Reviewer implements Reviewer, ApplicationListener<ContextRefreshedEvent> {
    private TestFunctionalityDAO testFunctionalityDAO;
    private TestingFunctionalityManager testFunctionalityManager;
    private ErrorMessageUtil msgUtil;
    private CertificationEditionDAO editionDAO;
    private List<CertificationEditionDTO> editionDTOs;

    @Autowired
    public TestFunctionality2014Reviewer(TestFunctionalityDAO testFunctionalityDAO,
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
    public void review(final CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null) {
            for (CertificationResult cr : listing.getCertificationResults()) {
                if (cr.getTestFunctionality() != null) {
                    for (CertificationResultTestFunctionality crtf : cr.getTestFunctionality()) {
                        listing.getErrorMessages().addAll(
                                getTestingFunctionalityErrorMessages(crtf, cr, listing));
                    }
                }
            }
        }
    }

    private Set<String> getTestingFunctionalityErrorMessages(final CertificationResultTestFunctionality crtf,
            final CertificationResult cr, final CertifiedProductSearchDetails listing) {

        Set<String> errors = new HashSet<String>();

        CertificationEditionDTO edition = getEditionDTO(getEditionFromListing(listing));
        TestFunctionalityDTO tf = getTestFunctionality(crtf.getName(), edition.getId());

        Long practiceTypeId = Long.valueOf(listing.getPracticeType().get("id").toString());
        if (!isTestFunctionalityPracticeTypeValid(practiceTypeId, tf)) {
            errors.add(getTestFunctionalityPracticeTypeErrorMessage(crtf, cr, listing));
        }

        if (!isTestFunctionalityCritierionValid(cr.getCriterion().getId(), tf, edition.getYear())) {
            errors.add(getTestFunctionalityCriterionErrorMessage(crtf, cr, listing, edition));
        }
        return errors;
    }

    private Boolean isTestFunctionalityCritierionValid(Long criteriaId, TestFunctionalityDTO tf, String year) {

        List<TestFunctionalityDTO> validTestFunctionalityForCriteria =
                testFunctionalityManager.getTestFunctionalityCriteriaMap2014().get(criteriaId);

        //Is the TestFunctionalityDTO in the valid list (relies on the TestFunctionalityDTO.equals()
        if (validTestFunctionalityForCriteria == null) {
            return false;
        } else {
            return validTestFunctionalityForCriteria.contains(tf);
        }
}

    private Boolean isTestFunctionalityPracticeTypeValid(final Long practiceTypeId, final TestFunctionalityDTO tf) {

        if (tf.getPracticeType() != null) {
            if (!practiceTypeId.equals(tf.getPracticeType().getId())) {
                return false;
            }
        }
        return true;
    }

    private String getTestFunctionalityPracticeTypeErrorMessage(final CertificationResultTestFunctionality crtf,
            final CertificationResult cr, final CertifiedProductSearchDetails cp) {
        CertificationEditionDTO editionDTO = getEditionDTO(getEditionFromListing(cp));
        TestFunctionalityDTO tf = getTestFunctionality(crtf.getName(), editionDTO.getId());

        return getTestFunctionalityPracticeTypeErrorMessage(
                cr.getNumber(),
                crtf.getName(),
                tf.getPracticeType().getName(),
                cp.getPracticeType().get("name").toString());
    }

    private String getTestFunctionalityPracticeTypeErrorMessage(final String criteriaNumber,
            final String testFunctionalityNumber, final String validPracticeTypeName,
            final String currentPracticeTypeName) {

        return msgUtil.getMessage("listing.criteria.testFunctionalityPracticeTypeMismatch",
                criteriaNumber, testFunctionalityNumber, validPracticeTypeName, currentPracticeTypeName);
    }

    private String getTestFunctionalityCriterionErrorMessage(final CertificationResultTestFunctionality crtf,
            final CertificationResult cr, final CertifiedProductSearchDetails cp,
            final CertificationEditionDTO edition) {

        TestFunctionalityDTO tf = getTestFunctionality(crtf.getName(), edition.getId());
        return getTestFunctionalityCriterionErrorMessage(
                cr.getNumber(),
                crtf.getName(),
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

    private String getEditionFromListing(final CertifiedProductSearchDetails listing) {
        String edition = "";
        if (listing.getCertificationEdition().containsKey(CertifiedProductSearchDetails.EDITION_NAME_KEY)) {
            edition = listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString();
        }
        return edition;
    }

    private TestFunctionalityDTO getTestFunctionality(final String number, final Long editionId) {
        return testFunctionalityDAO.getByNumberAndEdition(number, editionId);
    }

    private String getDelimitedListOfValidCriteriaNumbers(final TestFunctionalityDTO tfDTO,
            final CertificationEditionDTO edition) {

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
}
