package gov.healthit.chpl.app.permissions.domain;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.permissions.domains.FilterDomainPermissions;
import gov.healthit.chpl.permissions.domains.filter.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.filter.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.filter.GetByFilterTypeActionPermissions;
import gov.healthit.chpl.permissions.domains.filter.UpdateActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class FilterDomainPermissionsTest {
    @Autowired
    private FilterDomainPermissions permissions;

    @Test
    public void setupTest() {
        assertTrue(permissions.getActionPermissions().size() == 5);

        assertTrue(permissions.getActionPermissions()
                .get(FilterDomainPermissions.CREATE) instanceof CreateActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(FilterDomainPermissions.UPDATE) instanceof UpdateActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(FilterDomainPermissions.DELETE) instanceof DeleteActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(FilterDomainPermissions.GET_BY_FILTER_TYPE) instanceof GetByFilterTypeActionPermissions);
    }
}
