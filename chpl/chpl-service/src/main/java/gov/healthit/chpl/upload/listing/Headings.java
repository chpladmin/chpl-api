package gov.healthit.chpl.upload.listing;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public enum Headings {
    //while each heading can have multiple values that it matches to,
    //the values have to be unique among all headings for this to work
    UNIQUE_ID("UNIQUE_CHPL_ID__C", "UNIQUE_CHPL_ID", "CHPL_PRODUCT_NUMBER"),
    RECORD_STATUS("RECORD_STATUS__C", "RECORD_STATUS"),
    DEVELOPER("VENDOR__C", "VENDOR", "DEVELOPER__C", "DEVELOPER");

    private List<String> colNames;
    Headings(String...csvColNames) {
        colNames = new ArrayList<String>();
        for (String csvColName : csvColNames) {
            colNames.add(csvColName);
        }
    }

    public String getNamesAsString() {
        String names = "";
        for (int i = 0; i < this.colNames.size(); i++) {
            names += this.colNames.get(i);
            if (i < this.colNames.size() - 1) {
                names += ", ";
            }
            if (i == this.colNames.size() - 1) {
                names += " or ";
            }
        }
        return names;
    }

    public static Headings getHeading(String colName) {
        if (StringUtils.isEmpty(colName)) {
            return null;
        }
        for (Headings heading : values()) {
            if (heading.colNames.stream()
                    .anyMatch(headingColName -> headingColName.equalsIgnoreCase(colName.trim()))) {
                return heading;
            }
        }
        return null;
    }
}
