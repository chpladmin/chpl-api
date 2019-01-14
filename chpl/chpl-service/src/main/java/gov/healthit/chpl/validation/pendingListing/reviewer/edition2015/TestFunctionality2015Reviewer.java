package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
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
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

/**
 * Confirms that the test functionality is valid for the criteria that is applying it.
 * @author kekey
 *
 */
@Component("pendingTestFunctionality2015Reviewer")
@DependsOn("certificationEditionDAO")
public class TestFunctionality2015Reviewer implements Reviewer {//, ApplicationListener<ContextRefreshedEvent> {
    private TestFunctionalityDAO testFunctionalityDAO;
    private TestingFunctionalityManager testFunctionalityManager;
    private ErrorMessageUtil msgUtil;
    private CertificationEditionDAO editionDAO;
    private List<CertificationEditionDTO> editionDTOs;

    @Autowired
    public TestFunctionality2015Reviewer(TestFunctionalityDAO testFunctionalityDAO,
            TestingFunctionalityManager testFunctionalityManager, CertificationEditionDAO editionDAO,
            ErrorMessageUtil msgUtil) {
        this.testFunctionalityDAO = testFunctionalityDAO;
        this.testFunctionalityManager = testFunctionalityManager;
        this.editionDAO = editionDAO;
        this.msgUtil = msgUtil;
    }

    //    @Override
    //    public void onApplicationEvent(ContextRefreshedEvent event) {
    //        editionDTOs = editionDAO.findAll();
    //    }

    @PostConstruct
    public void init() {
        editionDTOs = editionDAO.findAll();
    }

    @Override
    public void review(final PendingCertifiedProductDTO listing) {
        if (listing.getCertificationCriterion() != null) {
            for (PendingCertificationResultDTO cr : listing.getCertificationCriterion()) {
                if (cr.getTestFunctionality() != null) {
                    Iterator<PendingCertificationResultTestFunctionalityDTO> crtfIter =
                            cr.getTestFunctionality().iterator();
                    while (crtfIter.hasNext()) {
                        PendingCertificationResultTestFunctionalityDTO crtf = crtfIter.next();
                        TestFunctionalityDTO tf =
                                getTestFunctionality(crtf.getNumber(), getEditionDTO(listing.getCertificationEdition()));
                        if (tf == null) {
                            listing.getErrorMessages().add(
                                    msgUtil.getMessage("listing.criteria.testFunctionalityNotFoundAndRemoved",
                                            cr.getNumber(), crtf.getNumber()));
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

    private Set<String> getTestingFunctionalityWarningMessages(final PendingCertificationResultTestFunctionalityDTO crtf,
            final PendingCertificationResultDTO cr, final PendingCertifiedProductDTO listing) {

        Set<String> warnings = new HashSet<String>();

        CertificationEditionDTO edition = getEditionDTO(getEditionFromListing(listing));
        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber(), edition.getId());

        String criterionNumber = cr.getNumber();
        if (!isTestFunctionalityCritierionValid(criterionNumber, tf, edition.getYear())) {
            warnings.add(getTestFunctionalityCriterionMessage(crtf, cr, listing, edition));
        }
        return warnings;
    }

    private Boolean isTestFunctionalityCritierionValid(final String criteriaNumber,
            final TestFunctionalityDTO tf, final String year) {

        List<TestFunctionalityDTO> validTestFunctionalityForCriteria =
                testFunctionalityManager.getTestFunctionalityCriteriaMap2015().get(criteriaNumber);

        if (validTestFunctionalityForCriteria == null) {
            return false;
        } else {
            //Is the TestFunctionalityDTO in the valid list (relies on the TestFunctionalityDTO.equals()
            return validTestFunctionalityForCriteria.contains(tf);
        }
    }

    private String getTestFunctionalityCriterionMessage(final PendingCertificationResultTestFunctionalityDTO crtf,
            final PendingCertificationResultDTO cr, final PendingCertifiedProductDTO cp,
            final CertificationEditionDTO edition) {

        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber(), edition.getId());
        return getMessage(
                cr.getNumber(),
                crtf.getNumber(),
                getDelimitedListOfValidCriteriaNumbers(tf, edition),
                cr.getNumber());
    }

    private String getMessage(final String criteriaNumber,
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

    private TestFunctionalityDTO getTestFunctionality(final String number, final CertificationEditionDTO edition) {
        return testFunctionalityDAO.getByNumberAndEdition(number, edition.getId());
    }
}
