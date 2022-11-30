package gov.healthit.chpl.util;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;

@SuppressWarnings("checkstyle:magicnumber")
public class DeprecatedFieldExplorerTest {

    private DeprecatedResponseFieldExplorer deprecatedFieldExplorer;

    @Before
    public void setup() {
        deprecatedFieldExplorer = new DeprecatedResponseFieldExplorer();
    }

    @Test
    public void findDeprecatedFields_All() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(TestableClass.class);
        assertNotNull(deprecatedItems);
        assertEquals(5, deprecatedItems.keySet().size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("attribute2"));
        assertTrue(deprecatedItemNames.contains("attribute3" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "attribute100"));
        assertTrue(deprecatedItemNames.contains("method1"));
        assertTrue(deprecatedItemNames.contains("testables1"));
        assertTrue(deprecatedItemNames.contains("testables2" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "attribute100"));
    }

    @Test
    public void findDeprecatedFields_SkipNonDeprecatedField_NotInResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(TestableClass.class);
        assertNotNull(deprecatedItems);
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertFalse(deprecatedItemNames.contains("attribute1"));
    }

    @Test
    public void findDeprecatedFields_SkipJsonIgnoreField_NotInResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(TestableClass.class);
        assertNotNull(deprecatedItems);
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertFalse(deprecatedItemNames.contains("attribute4"));
    }

    @Test
    public void findDeprecatedFields_SkipNonDeprecatedMethod_NotInResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(TestableClass.class);
        assertNotNull(deprecatedItems);
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertFalse(deprecatedItemNames.contains("method2"));
    }

    @Test
    public void findDeprecatedFields_SkipJsonPropertyWriteOnlyMethod_NotInResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(TestableClass.class);
        assertNotNull(deprecatedItems);
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertFalse(deprecatedItemNames.contains("method3"));
    }

    @Test
    public void findDeprecatedFields_SkipJsonIgnoreMethod_NotInResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(TestableClass.class);
        assertNotNull(deprecatedItems);
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertFalse(deprecatedItemNames.contains("method4"));
    }

    public class TestableClass {
        private Long attribute1;

        @DeprecatedResponseField(message = "message1", removalDate = "2022-12-15")
        private Long attribute2;

        private TestableSubclass attribute3;

        @JsonIgnore
        @DeprecatedResponseField(message = "message", removalDate = "2022-12-15")
        private String attribute4;

        @JsonProperty(access = Access.WRITE_ONLY)
        @DeprecatedResponseField(message = "message", removalDate = "2022-12-15")
        private String attribute5;

        @DeprecatedResponseField(removalDate = "2023-01-01",
            message = "message.")
        public String getMethod1() {
            return "";
        }

        public String getMethod2() {
            return "";
        }

        @JsonProperty(access = Access.WRITE_ONLY)
        @DeprecatedResponseField(message = "message", removalDate = "2022-12-15")
        public String getMethod3() {
            return "";
        }

        @JsonIgnore
        @DeprecatedResponseField(removalDate = "2023-01-01", message = "message.")
        public String getMethod4() {
            return "";
        }

        @DeprecatedResponseField(removalDate = "2023-01-01", message = "message.")
        private List<TestableSubclass> testables1;

        private List<TestableSubclass> testables2;
    }

    public class TestableSubclass {
        @DeprecatedResponseField(message = "message2", removalDate = "2022-12-15")
        private Long attribute100;
    }
}
