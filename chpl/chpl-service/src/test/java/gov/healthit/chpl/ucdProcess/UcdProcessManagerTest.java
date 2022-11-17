package gov.healthit.chpl.ucdProcess;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class UcdProcessManagerTest {
    private static final String UCD_PROCESS_IS_USED = "This UCD Process is attested to by %s listing%s.";
    private static final String EMPTY_NAME = "UCD Process Name is required.";
    private static final String DUPLICATE_NAME = "UCD Process Name '%s' matches an existing UCD Process.";

    private UcdProcessDAO ucdProcessDao;
    private ErrorMessageUtil msgUtil;
    private UcdProcessManager ucdProcessManager;

    @Before
    public void before() {
        ucdProcessDao = Mockito.mock(UcdProcessDAO.class);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("ucdProcess.delete.listingsExist"),
                ArgumentMatchers.anyInt(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(UCD_PROCESS_IS_USED, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("ucdProcess.emptyName")))
            .thenReturn(EMPTY_NAME);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("ucdProcess.duplicate"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(DUPLICATE_NAME, i.getArgument(1), ""));

        ucdProcessManager = new UcdProcessManager(ucdProcessDao, msgUtil);
    }

    @Test
    public void update_validUcdProcessWithNoChanges_NoErrorsAndUpdateNotCalledAndUcdProcessReturned() throws EntityRetrievalException, ValidationException {
        UcdProcess origUcdProcess = UcdProcess.builder()
                .id(1L)
                .name("UCD Process Name")
                .build();

        UcdProcess updatedUcdProcess = UcdProcess.builder()
                .id(1L)
                .name("UCD Process Name")
                .build();

        Mockito.when(ucdProcessDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(origUcdProcess);
        Mockito.when(ucdProcessDao.getAll()).thenReturn(Collections.EMPTY_LIST);

        UcdProcess returnedUcdProcess = ucdProcessManager.update(updatedUcdProcess);

        assertNotNull(returnedUcdProcess);
        Mockito.verify(ucdProcessDao, Mockito.times(0)).update(ArgumentMatchers.any(UcdProcess.class));
    }

    @Test
    public void update_validUcdProcessWithChanges_NoErrorsAndUpdateCalledAndUcdProcessReturned() throws EntityRetrievalException, ValidationException {
        UcdProcess origUcdProcess = UcdProcess.builder()
                .id(1L)
                .name("UCD Process Name")
                .build();

        UcdProcess updatedUcdProcess = UcdProcess.builder()
                .id(1L)
                .name("UCD Process Name Updated")
                .build();

        Mockito.when(ucdProcessDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(origUcdProcess);
        Mockito.when(ucdProcessDao.getAll()).thenReturn(Collections.EMPTY_LIST);
        Mockito.when(ucdProcessDao.update(updatedUcdProcess))
            .thenReturn(updatedUcdProcess);

        UcdProcess returnedUcdProcess = ucdProcessManager.update(updatedUcdProcess);

        assertNotNull(returnedUcdProcess);
        Mockito.verify(ucdProcessDao, Mockito.times(1)).update(ArgumentMatchers.any(UcdProcess.class));
    }

    @Test(expected = ValidationException.class)
    public void update_emptyName_throwsValidationException() throws EntityRetrievalException, ValidationException {
        UcdProcess origUcdProcess = UcdProcess.builder()
                .id(1L)
                .name("UCD Process Name")
                .build();

        UcdProcess updatedUcdProcess = UcdProcess.builder()
                .id(1L)
                .name("")
                .build();

        Mockito.when(ucdProcessDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(origUcdProcess);

        try {
            ucdProcessManager.update(updatedUcdProcess);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(EMPTY_NAME));
            throw ex;
        }
    }

    @Test(expected = ValidationException.class)
    public void update_blankName_throwsValidationException() throws EntityRetrievalException, ValidationException {
        UcdProcess origUcdProcess = UcdProcess.builder()
                .id(1L)
                .name("UCD Process Name")
                .build();

        UcdProcess updatedUcdProcess = UcdProcess.builder()
                .id(1L)
                .name("   ")
                .build();

        Mockito.when(ucdProcessDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(origUcdProcess);

        try {
            ucdProcessManager.update(updatedUcdProcess);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(EMPTY_NAME));
            throw ex;
        }
    }

    @Test(expected = ValidationException.class)
    public void update_nullName_throwsValidationException() throws EntityRetrievalException, ValidationException {
        UcdProcess origUcdProcess = UcdProcess.builder()
                .id(1L)
                .name("UCD Process Name")
                .build();

        UcdProcess updatedUcdProcess = UcdProcess.builder()
                .id(1L)
                .name(null)
                .build();

        Mockito.when(ucdProcessDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(origUcdProcess);

        try {
            ucdProcessManager.update(updatedUcdProcess);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(EMPTY_NAME));
            throw ex;
        }
    }

    @Test(expected = ValidationException.class)
    public void update_duplicateName_throwsValidationException() throws EntityRetrievalException, ValidationException {
        UcdProcess origUcdProcess = UcdProcess.builder()
                .id(1L)
                .name("UCD Process Name")
                .build();

        UcdProcess updatedUcdProcess = UcdProcess.builder()
                .id(1L)
                .name("Different UCD Name")
                .build();

        UcdProcess otherUcdProcess = UcdProcess.builder()
                .id(2L)
                .name("Different UCD Name")
                .build();

        Mockito.when(ucdProcessDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(origUcdProcess);
        Mockito.when(ucdProcessDao.getAll())
            .thenReturn(Stream.of(origUcdProcess, otherUcdProcess).toList());

        try {
            ucdProcessManager.update(updatedUcdProcess);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(DUPLICATE_NAME, "Different UCD Name")));
            throw ex;
        }
    }

    @Test
    public void create_validUcdProcess_NoErrorsAndCreateCalledAndUcdProcessReturned() throws EntityCreationException, ValidationException {
        UcdProcess newUcdProcess = UcdProcess.builder()
                .name("UCD Process Name")
                .build();

        Mockito.when(ucdProcessDao.getAll()).thenReturn(Collections.EMPTY_LIST);
        Mockito.when(ucdProcessDao.create(newUcdProcess)).thenReturn(newUcdProcess);

        UcdProcess returnedUcdProcess = ucdProcessManager.create(newUcdProcess);

        assertNotNull(returnedUcdProcess);
        assertEquals("UCD Process Name", returnedUcdProcess.getName());
        Mockito.verify(ucdProcessDao, Mockito.times(1)).create(ArgumentMatchers.any(UcdProcess.class));
    }

    @Test(expected = ValidationException.class)
    public void create_emptyName_throwsValidationException() throws EntityCreationException, ValidationException {
        UcdProcess newUcdProcess = UcdProcess.builder()
                .name("")
                .build();

        try {
            ucdProcessManager.create(newUcdProcess);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(EMPTY_NAME));
            throw ex;
        }
    }

    @Test(expected = ValidationException.class)
    public void create_blankName_throwsValidationException() throws EntityCreationException, ValidationException {
        UcdProcess newUcdProcess = UcdProcess.builder()
                .name("  ")
                .build();

        try {
            ucdProcessManager.create(newUcdProcess);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(EMPTY_NAME));
            throw ex;
        }
    }

    @Test(expected = ValidationException.class)
    public void create_nullName_throwsValidationException() throws EntityCreationException, ValidationException {
        UcdProcess newUcdProcess = UcdProcess.builder()
                .name(null)
                .build();

        try {
            ucdProcessManager.create(newUcdProcess);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(EMPTY_NAME));
            throw ex;
        }
    }

    @Test(expected = ValidationException.class)
    public void create_duplicateName_throwsValidationException() throws EntityCreationException, ValidationException {
        UcdProcess newUcdProcess = UcdProcess.builder()
                .name("UCD Process Name")
                .build();

        UcdProcess ucdProcess1 = UcdProcess.builder()
                .id(1L)
                .name("UCD Process Name")
                .build();

        UcdProcess ucdProcess2 = UcdProcess.builder()
                .id(2L)
                .name("Different UCD Name")
                .build();

        Mockito.when(ucdProcessDao.getAll())
            .thenReturn(Stream.of(ucdProcess1, ucdProcess2).toList());

        try {
            ucdProcessManager.create(newUcdProcess);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(DUPLICATE_NAME, "UCD Process Name")));
            throw ex;
        }
    }

    @Test
    public void delete_ValidAndNoListingsUsingIt_NoErrorsAndsDeleteCalled() throws EntityRetrievalException, ValidationException {
        UcdProcess toDelete = UcdProcess.builder()
                .id(1L)
                .name("UCD Process Name")
                .build();

        Mockito.when(ucdProcessDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(toDelete);
        Mockito.when(ucdProcessDao.getCertifiedProductsByUcdProcess(ArgumentMatchers.any(UcdProcess.class)))
            .thenReturn(Collections.EMPTY_LIST);
        Mockito.doNothing().when(ucdProcessDao).delete(ArgumentMatchers.eq(1L));
        ucdProcessManager.delete(toDelete.getId());
        Mockito.verify(ucdProcessDao, Mockito.times(1)).delete(ArgumentMatchers.eq(1L));
    }

    @Test(expected = ValidationException.class)
    public void delete_UcdProcessAssociatedWith1Listing_ValidationException() throws EntityRetrievalException, ValidationException {
        UcdProcess toDelete = UcdProcess.builder()
                .id(1L)
                .name("UCD Process Name")
                .build();

        Mockito.when(ucdProcessDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(toDelete);
        Mockito.when(ucdProcessDao.getCertifiedProductsByUcdProcess(ArgumentMatchers.any(UcdProcess.class)))
            .thenReturn(Stream.of(CertifiedProduct.builder().chplProductNumber("123456").build()).toList());
        try {
            ucdProcessManager.delete(toDelete.getId());
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(UCD_PROCESS_IS_USED, "1", "") + " 123456"));
            throw ex;
        }
    }

    @Test(expected = ValidationException.class)
    public void delete_UcdProcessAssociatedWith2Listings_ValidationException() throws EntityRetrievalException, ValidationException {
        UcdProcess toDelete = UcdProcess.builder()
                .id(1L)
                .name("UCD Process Name")
                .build();

        Mockito.when(ucdProcessDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(toDelete);
        Mockito.when(ucdProcessDao.getCertifiedProductsByUcdProcess(ArgumentMatchers.any(UcdProcess.class)))
            .thenReturn(Stream.of(CertifiedProduct.builder().chplProductNumber("1").build(),
                    CertifiedProduct.builder().chplProductNumber("2").build()).toList());
        try {
            ucdProcessManager.delete(toDelete.getId());
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(UCD_PROCESS_IS_USED, "2", "s") + " 1, 2"));
            throw ex;
        }
    }
}
