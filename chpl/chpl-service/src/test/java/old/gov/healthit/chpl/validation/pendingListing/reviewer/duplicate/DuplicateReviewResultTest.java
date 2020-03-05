package old.gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import java.util.function.BiPredicate;

import org.junit.Test;

import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.DuplicateReviewResult;

public class DuplicateReviewResultTest {
    @Test
    public void test() {
        BiPredicate<String, String> predicate =
                new BiPredicate<String, String>() {
            @Override
            public boolean test(String str1, String str2) {
                if (str1!= null && str2 != null) {
                    return str1.equals(str2);
                } else {
                    return false;
                }
            }
        };

        DuplicateReviewResult<String> drr = new DuplicateReviewResult<String>(predicate);

        drr.addObject("Test1");
        drr.addObject("Test2");
        drr.addObject("Test3");
        drr.addObject("Test1");
        drr.addObject("Test4");

        assertEquals(4, drr.getUniqueList().size());
        assertEquals(1, drr.getDuplicateList().size());
        assertEquals(true, drr.duplicatesExist());
    }
}
