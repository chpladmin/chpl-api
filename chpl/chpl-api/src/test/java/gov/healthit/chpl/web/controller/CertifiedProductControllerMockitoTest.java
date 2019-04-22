package gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.validation.listing.Edition2015ListingValidator;
import gov.healthit.chpl.validation.listing.ListingValidatorFactory;
import gov.healthit.chpl.validation.listing.reviewer.ChplNumberReviewer;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

public class CertifiedProductControllerMockitoTest {
    @Mock
    private CertificationResultManager certificationResultManager;

    @Spy
    @InjectMocks
    private Edition2015ListingValidator edition2015Validator;

    @Mock
    private ChplNumberReviewer chplNumberReviewer;

    @Mock
    private ChplProductNumberUtil chplProductNumberUtil;

    @Mock
    private CertifiedProductDetailsManager cpdManager;

    @Mock
    ListingValidatorFactory valFactory;

    @InjectMocks
    private CertifiedProductController myController;

    @Before
    public void setUp() throws Exception {
          MockitoAnnotations.initMocks(this);
    }

    @Transactional
    @Test
    public void ChplProductNumberHasCorrectAdditionalSoftwareCodeWhenExists() throws EntityRetrievalException, EntityCreationException, IOException {
        //This required some "deep" mocking...
        //CertifiedProductController
        //      -> Inject Mock of CertifiedProductDetailsManager
        //          -> mock method getCertifiedPreoductDetailsBasic to return a defined object
        //      -> Inject Mock of CertifiedProductValidatorFactory
        //          -> Mock Method getValidator to return instance of MyValidator
        //MyValidator Spy (local class which extends CertifiedProductValidatorImpl)
        //      -> Spy Method validateUniqueId to return true (since we're not testing this...)
        //      -> Inject Mock of CertificationResultManager
        //          -> Mock Method getCertifiedProductHasAdditionalSoftware to return true
        //Use doReturn(...).when(spy).method(...) when setting up spies
        //only return one reviewer for the chpl product number since that is where the
        //additional software code check happens
        List<Reviewer> reviewers = new ArrayList<Reviewer>();
        reviewers.add(chplNumberReviewer);
        doReturn(reviewers).when(edition2015Validator).getReviewers();

        //use when(mock.method(...)).thenReturn(...) when setting up mocks
        when(chplProductNumberUtil.isUnique(ArgumentMatchers.anyString())).thenReturn(false);
        when(certificationResultManager.getCertifiedProductHasAdditionalSoftware(anyLong())).thenReturn(true);
        when(cpdManager.getCertifiedProductDetailsBasic(anyLong())).thenReturn(getCertifiedProductBasicForAdditionalSoftwareTest());
        when(valFactory.getValidator(any(CertifiedProductSearchDetails.class))).thenReturn(edition2015Validator);

        CertifiedProductSearchDetails cp = myController.getCertifiedProductByIdBasic(8252L);

        assertEquals("15.04.04.2945.Ligh.21.00.1.161229", cp.getChplProductNumber());
    }

    private CertifiedProductSearchDetails getCertifiedProductBasicForAdditionalSoftwareTest() {
        CertifiedProductSearchDetails cp = new CertifiedProductSearchDetails();
        cp.setId(8252L);
        cp.setChplProductNumber("15.04.04.2945.Ligh.21.00.1.161229");
        cp.setDeveloper(new Developer());
        cp.getDeveloper().setDeveloperCode("2495");
        cp.setCertificationEdition(new HashMap<String, Object>());
        cp.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        cp.setCertifyingBody(new HashMap<String, Object>());
        cp.getCertifyingBody().put("code", "04");
        cp.getCertifyingBody().put("name", "Drummond Group");
        cp.setTestingLabs(new ArrayList<CertifiedProductTestingLab>());
        cp.getTestingLabs().add(new CertifiedProductTestingLab());
        cp.getTestingLabs().get(0).setTestingLabName("Drummond Group");
        cp.getTestingLabs().get(0).setTestingLabCode("04");
        cp.setCertificationDate(1482987600000L);
        return cp;
    }
}
