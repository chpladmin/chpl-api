package gov.healthit.chpl.upload.listing;

import java.util.List;

public interface Heading {
    public List<String> getColNames();
    public String getHeading();
    public String getNamesAsString();
}
