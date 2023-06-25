package gov.healthit.chpl.svap.manager;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.svap.dao.SvapDAO;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.FileUtils;

public class SvapManagerTest {
    private SvapDAO svapDao;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDAO;
    private ErrorMessageUtil errorMessageUtil;
    private SvapManager svapManager;

    @Before
    public void before() {
        svapDao = Mockito.mock(SvapDAO.class);
        certificationCriterionAttributeDAO = Mockito.mock(CertificationCriterionAttributeDAO.class);
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        FileUtils fileUtils = new FileUtils(Mockito.mock(Environment.class), errorMessageUtil);
        svapManager = new SvapManager(svapDao, fileUtils,
                errorMessageUtil, certificationCriterionAttributeDAO, "report", "reportSchema");
    }

    @Test
    public void update_ValidSvapWithNoChangesToCriteria_NoErrorsAndUpdateCalledAndSvapReturned() throws EntityRetrievalException, ValidationException {
        Svap origSvap = Svap.builder()
                .svapId(1L)
                .approvedStandardVersion("This is the long text.")
                .regulatoryTextCitation("170.205(a)(10)")
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315(a)(1)")
                        .build())
                .build();

        Svap updatedSvap = origSvap.toBuilder()
                .approvedStandardVersion("This is an editted long text")
                .build();

        Mockito.when(svapDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(origSvap);
        Mockito.when(svapDao.update(ArgumentMatchers.any(Svap.class)))
                .thenReturn(updatedSvap.toBuilder().build());

        Svap savedSvap = svapManager.update(updatedSvap);

        assertNotNull(savedSvap);
        Mockito.verify(svapDao, Mockito.times(1)).update(ArgumentMatchers.any(Svap.class));
    }

    @Test
    public void update_ValidSvapWithAddedCriteria_NoErrorsAndUpdateCalledAndSvapReturned() throws EntityRetrievalException, ValidationException {
        Svap origSvap = Svap.builder()
                .svapId(1L)
                .approvedStandardVersion("This is the long text.")
                .regulatoryTextCitation("170.205(a)(10)")
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315(a)(1)")
                        .build())
                .build();

        Svap updatedSvap = origSvap.toBuilder()
                .criterion(CertificationCriterion.builder()
                        .id(2L)
                        .number("170.315(a)(2)")
                        .build())
                .build();

        Mockito.when(svapDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(origSvap);
        Mockito.when(svapDao.update(ArgumentMatchers.any(Svap.class)))
                .thenReturn(updatedSvap.toBuilder().build());

        Svap savedSvap = svapManager.update(updatedSvap);

        assertNotNull(savedSvap);
        Mockito.verify(svapDao, Mockito.times(1)).update(ArgumentMatchers.any(Svap.class));
    }

    @Test
    public void update_ValidSvapWithRemovedCriteria_NoErrorsAndUpdateCalledAndSvapReturned() throws EntityRetrievalException, ValidationException {
        Svap origSvap = Svap.builder()
                .svapId(1L)
                .approvedStandardVersion("This is the long text.")
                .regulatoryTextCitation("170.205(a)(10)")
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315(a)(1)")
                        .build())
                .criterion(CertificationCriterion.builder()
                        .id(2L)
                        .number("170.315(a)(2)")
                        .build())
                .build();

        Svap updatedSvap = origSvap.toBuilder().build();
        updatedSvap.setCriteria(new ArrayList<CertificationCriterion>(updatedSvap.getCriteria()));
        updatedSvap.getCriteria().remove(0);

        Mockito.when(svapDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(origSvap);
        Mockito.when(svapDao.update(ArgumentMatchers.any(Svap.class)))
                .thenReturn(updatedSvap.toBuilder().build());

        Svap savedSvap = svapManager.update(updatedSvap);

        assertNotNull(savedSvap);
        Mockito.verify(svapDao, Mockito.times(1)).update(ArgumentMatchers.any(Svap.class));
    }

    @Test(expected = ValidationException.class)
    public void update_EmptyRegTextCit_ValidationException() throws EntityRetrievalException, ValidationException {
        Svap origSvap = Svap.builder()
                .svapId(1L)
                .approvedStandardVersion("This is the long text.")
                .regulatoryTextCitation("170.205(a)(10)")
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315(a)(1)")
                        .build())
                .build();

        Svap updatedSvap = origSvap.toBuilder()
                .regulatoryTextCitation("")
                .build();

        Mockito.when(svapDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(origSvap);
        Mockito.when(svapDao.update(ArgumentMatchers.any(Svap.class)))
                .thenReturn(updatedSvap.toBuilder().build());

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
                .thenReturn("Error Message");

        svapManager.update(updatedSvap);
    }

    @Test(expected = ValidationException.class)
    public void update_EmptyAppStdVer_ValidationException() throws EntityRetrievalException, ValidationException {
        Svap origSvap = Svap.builder()
                .svapId(1L)
                .approvedStandardVersion("This is the long text.")
                .regulatoryTextCitation("170.205(a)(10)")
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315(a)(1)")
                        .build())
                .build();

        Svap updatedSvap = origSvap.toBuilder()
                .approvedStandardVersion("")
                .build();

        Mockito.when(svapDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(origSvap);
        Mockito.when(svapDao.update(ArgumentMatchers.any(Svap.class)))
                .thenReturn(updatedSvap.toBuilder().build());

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString())).thenReturn("Error Message");

        svapManager.update(updatedSvap);
    }

    @Test(expected = ValidationException.class)
    public void update_NoCriteriaAssoc_ValidationException() throws EntityRetrievalException, ValidationException {
        Svap origSvap = Svap.builder()
                .svapId(1L)
                .approvedStandardVersion("This is the long text.")
                .regulatoryTextCitation("170.205(a)(10)")
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315(a)(1)")
                        .build())
                .build();

        Svap updatedSvap = origSvap.toBuilder().build();
        updatedSvap.setCriteria(new ArrayList<CertificationCriterion>(updatedSvap.getCriteria()));
        updatedSvap.getCriteria().remove(0);

        Mockito.when(svapDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(origSvap);
        Mockito.when(svapDao.update(ArgumentMatchers.any(Svap.class)))
                .thenReturn(updatedSvap.toBuilder().build());

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString())).thenReturn("Error Message");

        svapManager.update(updatedSvap);
    }

    @Test(expected = ValidationException.class)
    public void update_RemovedCriteriaAssocWithListing_ValidationException() throws EntityRetrievalException, ValidationException {
        Svap origSvap = Svap.builder()
                .svapId(1L)
                .approvedStandardVersion("This is the long text.")
                .regulatoryTextCitation("170.205(a)(10)")
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315(a)(1)")
                        .title("Cures Update")
                        .build())
                .criterion(CertificationCriterion.builder()
                        .id(2L)
                        .number("170.315(a)(2)")
                        .title("Cures Update")
                        .build())
                .build();

        Svap updatedSvap = origSvap.toBuilder().build();
        updatedSvap.setCriteria(new ArrayList<CertificationCriterion>(updatedSvap.getCriteria()));
        updatedSvap.getCriteria().remove(0);

        Mockito.when(svapDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(origSvap);
        Mockito.when(svapDao.update(ArgumentMatchers.any(Svap.class)))
                .thenReturn(updatedSvap.toBuilder().build());
        Mockito.when(svapDao.getCertifiedProductsBySvapAndCriteria(ArgumentMatchers.any(Svap.class), ArgumentMatchers.any(CertificationCriterion.class)))
                .thenReturn(Arrays.asList(new CertifiedProductDetailsDTO()));

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
                .thenReturn("Error Message");

        svapManager.update(updatedSvap);
    }

    @Test
    public void create_ValidSvapWithNoChangesToCriteria_NoErrorsAndUpdateCalledAndSvapReturned() throws EntityRetrievalException, ValidationException {
        Svap newSvap = Svap.builder()
                .svapId(1L)
                .approvedStandardVersion("This is the long text.")
                .regulatoryTextCitation("170.205(a)(10)")
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315(a)(1)")
                        .build())
                .build();

        Mockito.when(svapDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(newSvap);
        Mockito.when(svapDao.create(ArgumentMatchers.any(Svap.class)))
                .thenReturn(newSvap.toBuilder().build());

        Svap savedSvap = svapManager.create(newSvap);

        assertNotNull(savedSvap);
        Mockito.verify(svapDao, Mockito.times(1)).create(ArgumentMatchers.any(Svap.class));
    }

    @Test(expected = ValidationException.class)
    public void create_EmptyRegTextCit_ValidationException() throws EntityRetrievalException, ValidationException {
        Svap newSvap = Svap.builder()
                .svapId(1L)
                .approvedStandardVersion("This is the long text.")
                .regulatoryTextCitation("")
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315(a)(1)")
                        .build())
                .build();

        Mockito.when(svapDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(newSvap);
        Mockito.when(svapDao.create(ArgumentMatchers.any(Svap.class)))
                .thenReturn(newSvap.toBuilder().build());

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
                .thenReturn("Error Message");

        svapManager.update(newSvap);
    }

    @Test(expected = ValidationException.class)
    public void create_EmptyAppStdVer_ValidationException() throws EntityRetrievalException, ValidationException {
        Svap newSvap = Svap.builder()
                .svapId(1L)
                .approvedStandardVersion("")
                .regulatoryTextCitation("170.205(a)(10)")
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315(a)(1)")
                        .build())
                .build();

        Mockito.when(svapDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(newSvap);
        Mockito.when(svapDao.create(ArgumentMatchers.any(Svap.class)))
                .thenReturn(newSvap.toBuilder().build());

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString())).thenReturn("Error Message");

        svapManager.create(newSvap);
    }

    @Test(expected = ValidationException.class)
    public void create_NoCriteriaAssoc_ValidationException() throws EntityRetrievalException, ValidationException {
        Svap newSvap = Svap.builder()
                .svapId(1L)
                .approvedStandardVersion("This is the long text.")
                .regulatoryTextCitation("170.205(a)(10)")
                .build();

        Mockito.when(svapDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(newSvap);
        Mockito.when(svapDao.update(ArgumentMatchers.any(Svap.class)))
                .thenReturn(newSvap.toBuilder().build());

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString())).thenReturn("Error Message");

        svapManager.update(newSvap);
    }

    @Test
    public void delete_Valid_NoErrorsAndsDeleteCalled() throws EntityRetrievalException, ValidationException {
        Svap svap = Svap.builder()
                .svapId(1L)
                .approvedStandardVersion("This is the long text.")
                .regulatoryTextCitation("170.205(a)(10)")
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315(a)(1)")
                        .title("Cures Update")
                        .build())
                .criterion(CertificationCriterion.builder()
                        .id(2L)
                        .number("170.315(a)(2)")
                        .title("Cures Update")
                        .build())
                .build();

        Mockito.when(svapDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(svap);
        Mockito.doNothing().when(svapDao).remove(ArgumentMatchers.any(Svap.class));

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString())).thenReturn("Error Message");

        svapManager.delete(svap.getSvapId());
    }

    @Test(expected = ValidationException.class)
    public void delete_SvapAssocWithListing_ValidationException() throws EntityRetrievalException, ValidationException {
        Svap svap = Svap.builder()
                .svapId(1L)
                .approvedStandardVersion("This is the long text.")
                .regulatoryTextCitation("170.205(a)(10)")
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315(a)(1)")
                        .title("Cures Update")
                        .build())
                .criterion(CertificationCriterion.builder()
                        .id(2L)
                        .number("170.315(a)(2)")
                        .title("Cures Update")
                        .build())
                .build();

        Mockito.when(svapDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(svap);
        Mockito.doNothing().when(svapDao).remove(ArgumentMatchers.any(Svap.class));
        Mockito.when(svapDao.getCertifiedProductsBySvap(ArgumentMatchers.any(Svap.class)))
                .thenReturn(Arrays.asList(new CertifiedProductDetailsDTO()));

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
                .thenReturn("Error Message");

        svapManager.delete(svap.getSvapId());
    }

}
