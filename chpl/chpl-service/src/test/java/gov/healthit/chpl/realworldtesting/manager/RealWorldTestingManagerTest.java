package gov.healthit.chpl.realworldtesting.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.UUID;

import org.ff4j.FF4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.quartz.SchedulerException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.realworldtesting.dao.RealWorldTestingByDeveloperDao;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUploadResponse;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class RealWorldTestingManagerTest {
    private RealWorldTestingManager realWorldTestingManager;
    private SchedulerManager schedulerManager;
    private ErrorMessageUtil errorMessageUtil;

    @BeforeEach
    public void setup() throws SchedulerException, ValidationException, UserRetrievalException {
        setSecurityContext();

        schedulerManager = Mockito.mock(SchedulerManager.class);
        Mockito.when(schedulerManager.createBackgroundJobTrigger(ArgumentMatchers.any(ChplOneTimeTrigger.class)))
                .thenReturn(new ChplOneTimeTrigger());

        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any(Object[].class)))
            .thenReturn("This is an error message.");

        realWorldTestingManager = new RealWorldTestingManager(Mockito.mock(RealWorldTestingByDeveloperDao.class),
                schedulerManager,
                errorMessageUtil,
                Mockito.mock(FF4j.class));
    }

    @Test
    public void uploadRealWorldTestingCsv_EmptyFile_ValidationException()
            throws ValidationException, SchedulerException, UserRetrievalException {
        String fileContents = "";

        MockMultipartFile file = new MockMultipartFile("rwt.csv",
                "rwt.csv",
                "text/csv",
                fileContents.getBytes());

        Exception exception = assertThrows(ValidationException.class, () -> {
            realWorldTestingManager.uploadRealWorldTestingCsv(file);
        });
        assertNotNull(exception);
    }

    @Test
    public void uploadRealWorldTestingCsv_WrongFileType_ValidationException()
            throws ValidationException, SchedulerException, UserRetrievalException {
        String fileContents = "<root/>";

        MockMultipartFile file = new MockMultipartFile("rwt.xml",
                "rwt.xml",
                MediaType.APPLICATION_XHTML_XML_VALUE,
                fileContents.getBytes());

        Exception exception = assertThrows(ValidationException.class, () -> {
            realWorldTestingManager.uploadRealWorldTestingCsv(file);
        });
        assertNotNull(exception);
    }

    @Test
    public void uploadRealWorldTestingCsv_FileOnlyHasHeader_ValidationException()
            throws ValidationException, SchedulerException, UserRetrievalException {

        String fileContents = "UNIQUE_CHPL_ID__C,TYPE,LAST_CHECKED,URL";
        MockMultipartFile file = new MockMultipartFile("rwt.csv",
                "rwt.csv",
                "text/csv",
                fileContents.getBytes());

        Exception exception = assertThrows(ValidationException.class, () -> {
            realWorldTestingManager.uploadRealWorldTestingCsv(file);
        });
        assertNotNull(exception);
    }

    @Test
    public void uploadRealWorldTestingCsv_FileWithHeaderAndDataRows_Success()
            throws ValidationException, SchedulerException, UserRetrievalException {

        String fileContents = "UNIQUE_CHPL_ID__C,TYPE,LAST_CHECKED,URL\r\n"
                + "15.04.04.3068.ACPl.01.00.0.200129,PLANS,20201001,https://www.abc.com\r\n"
                + "15.04.04.3068.ACPl.01.00.0.200129,RESULTS,20220202,https://www.abc2.com\r\n";

        MockMultipartFile file = new MockMultipartFile("rwt.csv",
                "rwt.csv",
                "text/csv",
                fileContents.getBytes());

        try (MockedStatic<AuthUtil> mockedAuthUtil = Mockito.mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getCurrentUser).thenReturn(getUser());

            RealWorldTestingUploadResponse response = realWorldTestingManager.uploadRealWorldTestingCsv(file);
            assertEquals("user@abc.com", response.getEmail());
            assertEquals("rwt.csv", response.getFileName());
            assertEquals(2,  response.getRecordsToBeProcessed());
        }
    }


    @Test
    public void uploadRealWorldTestingCsv_FileWithoutHeaderAndDataRows_Success()
            throws ValidationException, SchedulerException, UserRetrievalException {

        String fileContents = "15.04.04.3068.ACPl.01.00.0.200129,PLANS,20201001,https://www.abc.com\r\n"
                + "15.04.04.3068.ACPl.01.00.0.200129,RESULTS,20220202,https://www.abc2.com\r\n";

        MockMultipartFile file = new MockMultipartFile("rwt.csv",
                "rwt.csv",
                "text/csv",
                fileContents.getBytes());

        try (MockedStatic<AuthUtil> mockedAuthUtil = Mockito.mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getCurrentUser).thenReturn(getUser());
            RealWorldTestingUploadResponse response = realWorldTestingManager.uploadRealWorldTestingCsv(file);

            assertEquals("user@abc.com", response.getEmail());
            assertEquals("rwt.csv", response.getFileName());
            assertEquals(2,  response.getRecordsToBeProcessed());
        }
    }

    @Test
    public void uploadRealWorldTestingCsv_FileWithoutHeaderAndSingleDataRow_Success()
            throws ValidationException, SchedulerException, UserRetrievalException {

        String fileContents = "15.04.04.3068.ACPl.01.00.0.200129,PLANS,20201001,https://www.abc.com\r\n";

        MockMultipartFile file = new MockMultipartFile("rwt.csv",
                "rwt.csv",
                "text/csv",
                fileContents.getBytes());
        try (MockedStatic<AuthUtil> mockedAuthUtil = Mockito.mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getCurrentUser).thenReturn(getUser());

            RealWorldTestingUploadResponse response = realWorldTestingManager.uploadRealWorldTestingCsv(file);
            assertEquals("user@abc.com", response.getEmail());
            assertEquals("rwt.csv", response.getFileName());
            assertEquals(1,  response.getRecordsToBeProcessed());
        }
    }

    private JWTAuthenticatedUser getUser() {
        return JWTAuthenticatedUser.builder()
                .email("user@abc.com")
                .authorities(new ArrayList<GrantedAuthority>())
                .build();
    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setCognitoId(UUID.randomUUID());
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getAuthorities().add(new SimpleGrantedAuthority(CognitoGroups.CHPL_ADMIN));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
    }
}
