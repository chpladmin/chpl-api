package gov.healthit.chpl.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public class DeprecatedFieldExplorerTest {

    private DeprecatedFieldExplorer deprecatedFieldExplorer;

    @Before
    public void setup() {
        deprecatedFieldExplorer = new DeprecatedFieldExplorer();
    }

    @Test
    public void findDeprecatedFields_CertifiedProductSearchDetailsFindsTopLevel() {
        Set<String> deprecatedFieldNames = new LinkedHashSet<String>();
        deprecatedFieldExplorer.getAllDeprecatedFields(CertifiedProductSearchDetails.class, deprecatedFieldNames, "");

        assertNotNull(deprecatedFieldNames);
        assertTrue(deprecatedFieldNames.contains("transparencyAttestationUrl"));
        assertTrue(deprecatedFieldNames.contains("meaningfulUseUserHistory"));
        assertTrue(deprecatedFieldNames.contains("currentMeaningfulUseUsers"));

    }
}
