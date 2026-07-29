package gov.healthit.chpl;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

// TEMPORARY (ONC-5395): intentionally fails to verify docker-publish.yml
// gates the image build on a failing unit-tests job. Delete this file
// once the test build is confirmed to fail as expected.
public class TemporaryFailingTest {
    @Test
    public void intentionalFailure() {
        fail("Intentional failure to test docker-publish.yml unit-tests gate");
    }
}
