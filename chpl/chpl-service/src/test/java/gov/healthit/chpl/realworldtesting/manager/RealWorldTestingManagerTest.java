package gov.healthit.chpl.realworldtesting.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.quartz.SchedulerException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUploadResponse;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class RealWorldTestingManagerTest {
    private static final Long USER_ID = -2L;

    private RealWorldTestingManager realWorldTestingManager;
    private SchedulerManager schedulerManager;
    private UserManager userManager;
    private ErrorMessageUtil errorMessageUtil;

    @Before
    public void setup() throws SchedulerException, ValidationException, UserRetrievalException {
        setSecurityContext();

        schedulerManager = Mockito.mock(SchedulerManager.class);
        Mockito.when(schedulerManager.createBackgroundJobTrigger(ArgumentMatchers.any(ChplOneTimeTrigger.class)))
                .thenReturn(new ChplOneTimeTrigger());

        userManager = Mockito.mock(UserManager.class);
        Mockito.when(userManager.getById(ArgumentMatchers.anyLong()))
                .thenReturn(getUser());

        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        realWorldTestingManager = new RealWorldTestingManager(schedulerManager, userManager, errorMessageUtil);
    }

    @Test(expected = ValidationException.class)
    public void uploadRealWorldTestingCsv_EmptyFile_ValidationException()
            throws ValidationException, SchedulerException, UserRetrievalException {
        String fileContents = "";

        MockMultipartFile file = new MockMultipartFile("rwt.csv",
                "rwt.csv",
                "text/csv",
                fileContents.getBytes());

        realWorldTestingManager.uploadRealWorldTestingCsv(file);
    }

    @Test(expected = ValidationException.class)
    public void uploadRealWorldTestingCsv_WrongFileType_ValidationException()
            throws ValidationException, SchedulerException, UserRetrievalException {
        String fileContents = "<root/>";

        MockMultipartFile file = new MockMultipartFile("rwt.xml",
                "rwt.xml",
                MediaType.APPLICATION_XHTML_XML_VALUE,
                fileContents.getBytes());

        realWorldTestingManager.uploadRealWorldTestingCsv(file);
    }

    @Test(expected = ValidationException.class)
    public void uploadRealWorldTestingCsv_FileOnlyHasHeader_ValidationException()
            throws ValidationException, SchedulerException, UserRetrievalException {

        String fileContents = "UNIQUE_CHPL_ID__C,TYPE,LAST_CHECKED,URL";
        MockMultipartFile file = new MockMultipartFile("rwt.csv",
                "rwt.csv",
                "text/csv",
                fileContents.getBytes());

        realWorldTestingManager.uploadRealWorldTestingCsv(file);
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

        RealWorldTestingUploadResponse response = realWorldTestingManager.uploadRealWorldTestingCsv(file);

        assertEquals("user@abc.com", response.getEmail());
        assertEquals("rwt.csv", response.getFileName());
    }

    private UserDTO getUser() {
        UserDTO user = new UserDTO();
        user.setEmail("user@abc.com");
        return user;
    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(USER_ID);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(adminUser);
    }
}
