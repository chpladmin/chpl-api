package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.healthit.chpl.domain.ListingUpload;
import lombok.Data;

@Data
public class ListingUploadResponse implements Serializable {
    private static final long serialVersionUID = 1429960617156224833L;
    private List<ListingUpload> successfulListingUploads;
    private Set<String> errorMessages;
    private Set<String> warningMessages;

    public ListingUploadResponse() {
        successfulListingUploads = new ArrayList<ListingUpload>();
        errorMessages = new HashSet<String>();
        warningMessages = new HashSet<String>();
    }
}
