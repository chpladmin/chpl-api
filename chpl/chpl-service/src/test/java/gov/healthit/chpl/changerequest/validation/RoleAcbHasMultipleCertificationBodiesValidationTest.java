package gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;

public class RoleAcbHasMultipleCertificationBodiesValidationTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private RoleAcbHasMultipleCertificationBodiesValidation validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isValid_Success() throws EntityRetrievalException {
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin())
                .thenReturn(true);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser())
                .thenReturn(getAllAcbForUser(1l));

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertTrue(isValid);
    }

    @Test
    public void isValid_Success_NotRoleAcb() throws EntityRetrievalException {
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin())
                .thenReturn(false);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser())
                .thenReturn(getAllAcbForUser(1l));

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertTrue(isValid);
    }

    @Test
    public void isValid_Fail_MultipleAcbs() throws EntityRetrievalException {
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin())
                .thenReturn(true);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser())
                .thenReturn(getAllAcbForUser(1l, 33l));

        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                new ChangeRequestBuilder()
                        .withId(1l)
                        .build(),
                null);

        boolean isValid = validator.isValid(context);

        assertFalse(isValid);
    }

    private List<DeveloperDTO> getAllDeveloperForUser(Long... developerIds) {
        List<DeveloperDTO> dtos = new ArrayList<DeveloperDTO>();

        for (Long acbId : developerIds) {
            DeveloperDTO dto = new DeveloperDTO();
            dto.setId(acbId);
            dtos.add(dto);
        }
        return dtos;
    }

    private List<CertificationBodyDTO> getAllAcbForUser(Long... acbIds) {
        List<CertificationBodyDTO> dtos = new ArrayList<CertificationBodyDTO>();

        for (Long acbId : acbIds) {
            CertificationBodyDTO dto = new CertificationBodyDTO();
            dto.setId(acbId);
            dtos.add(dto);
        }

        return dtos;
    }

}
