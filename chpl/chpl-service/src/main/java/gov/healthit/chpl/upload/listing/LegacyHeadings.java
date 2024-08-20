package gov.healthit.chpl.upload.listing;

import java.util.ArrayList;
import java.util.List;

public enum LegacyHeadings {
    MACRA_MEASURE_G1("Measure Successfully Tested for G1"),
    MACRA_MEASURE_G2("Measure Successfully Tested for G2"),
    //This was always called "Standards" which is now used for "Standards"
    //but "Test Standards" are something different, so I'm not sure if we need this but maybe.
    TEST_STANDARD("Test Standards"),
    TEST_PROCEDURE("Test procedure"),
    TEST_PROCEDURE_VERSION("Test procedure version");

    private List<String> colNames;

    LegacyHeadings(String... csvColNames) {
        colNames = new ArrayList<String>();
        for (String csvColName : csvColNames) {
            colNames.add(csvColName);
        }
    }

    public String getHeading() {
        return this.colNames.get(0);
    }
}
