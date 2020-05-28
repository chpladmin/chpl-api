package gov.healthit.chpl.permissions.domain.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dto.FilterDTO;
import gov.healthit.chpl.dto.FilterTypeDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.filter.CreateActionPermissions;

public class CreateActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private CreateActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        FilterDTO filterDTO = new FilterDTO();
        filterDTO.setFilter("{}");
        filterDTO.setFilterType(new FilterTypeDTO());
        filterDTO.getFilterType().setId(1l);
        filterDTO.getFilterType().setName("SAMPLE_FILTER");
        filterDTO.setUser(new UserDTO());
        filterDTO.getUser().setId(-2l);
        assertTrue(permissions.hasAccess(filterDTO));

        filterDTO = new FilterDTO();
        filterDTO.setFilter("{}");
        filterDTO.setFilterType(new FilterTypeDTO());
        filterDTO.getFilterType().setId(1l);
        filterDTO.getFilterType().setName("SAMPLE_FILTER");
        filterDTO.setUser(new UserDTO());
        filterDTO.getUser().setId(-3l);
        assertFalse(permissions.hasAccess(filterDTO));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        FilterDTO filterDTO = new FilterDTO();
        filterDTO.setFilter("{}");
        filterDTO.setFilterType(new FilterTypeDTO());
        filterDTO.getFilterType().setId(1l);
        filterDTO.getFilterType().setName("SAMPLE_FILTER");
        filterDTO.setUser(new UserDTO());
        filterDTO.getUser().setId(3l);
        assertTrue(permissions.hasAccess(filterDTO));

        filterDTO = new FilterDTO();
        filterDTO.setFilter("{}");
        filterDTO.setFilterType(new FilterTypeDTO());
        filterDTO.getFilterType().setId(1l);
        filterDTO.getFilterType().setName("SAMPLE_FILTER");
        filterDTO.setUser(new UserDTO());
        filterDTO.getUser().setId(-3l);
        assertFalse(permissions.hasAccess(filterDTO));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        FilterDTO filterDTO = new FilterDTO();
        filterDTO.setFilter("{}");
        filterDTO.setFilterType(new FilterTypeDTO());
        filterDTO.getFilterType().setId(1l);
        filterDTO.getFilterType().setName("SAMPLE_FILTER");
        filterDTO.setUser(new UserDTO());
        filterDTO.getUser().setId(3l);
        assertTrue(permissions.hasAccess(filterDTO));

        filterDTO = new FilterDTO();
        filterDTO.setFilter("{}");
        filterDTO.setFilterType(new FilterTypeDTO());
        filterDTO.getFilterType().setId(1l);
        filterDTO.getFilterType().setName("SAMPLE_FILTER");
        filterDTO.setUser(new UserDTO());
        filterDTO.getUser().setId(-3l);
        assertFalse(permissions.hasAccess(filterDTO));
    }

    @Override
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        FilterDTO filterDTO = new FilterDTO();
        filterDTO.setFilter("{}");
        filterDTO.setFilterType(new FilterTypeDTO());
        filterDTO.getFilterType().setId(1l);
        filterDTO.getFilterType().setName("SAMPLE_FILTER");
        filterDTO.setUser(new UserDTO());
        filterDTO.getUser().setId(3l);
        assertTrue(permissions.hasAccess(filterDTO));

        filterDTO = new FilterDTO();
        filterDTO.setFilter("{}");
        filterDTO.setFilterType(new FilterTypeDTO());
        filterDTO.getFilterType().setId(1l);
        filterDTO.getFilterType().setName("SAMPLE_FILTER");
        filterDTO.setUser(new UserDTO());
        filterDTO.getUser().setId(-3l);
        assertFalse(permissions.hasAccess(filterDTO));
    }

    @Override
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        FilterDTO filterDTO = new FilterDTO();
        filterDTO.setFilter("{}");
        filterDTO.setFilterType(new FilterTypeDTO());
        filterDTO.getFilterType().setId(1l);
        filterDTO.getFilterType().setName("SAMPLE_FILTER");
        filterDTO.setUser(new UserDTO());
        filterDTO.getUser().setId(3l);
        assertTrue(permissions.hasAccess(filterDTO));

        filterDTO = new FilterDTO();
        filterDTO.setFilter("{}");
        filterDTO.setFilterType(new FilterTypeDTO());
        filterDTO.getFilterType().setId(1l);
        filterDTO.getFilterType().setName("SAMPLE_FILTER");
        filterDTO.setUser(new UserDTO());
        filterDTO.getUser().setId(-3l);
        assertFalse(permissions.hasAccess(filterDTO));
    }

    @Override
    public void hasAccess_Anon() throws Exception {
        assertFalse(permissions.hasAccess());

        FilterDTO filterDTO = new FilterDTO();
        filterDTO.setFilter("{}");
        filterDTO.setFilterType(new FilterTypeDTO());
        filterDTO.getFilterType().setId(1l);
        filterDTO.getFilterType().setName("SAMPLE_FILTER");
        filterDTO.setUser(new UserDTO());
        filterDTO.getUser().setId(-3l);
        assertFalse(permissions.hasAccess(filterDTO));
    }

}
