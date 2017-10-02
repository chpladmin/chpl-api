package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.List;

public class SedUpdateRequest {
    private List<TestTask> testTasks;

    public SedUpdateRequest() {
        this.testTasks = new ArrayList<TestTask>();
    }

    public List<TestTask> getTestTasks() {
        return testTasks;
    }

    public void setTestTasks(final List<TestTask> testTasks) {
        this.testTasks = testTasks;
    }
}
