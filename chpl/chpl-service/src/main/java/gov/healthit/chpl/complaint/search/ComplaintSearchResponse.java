package gov.healthit.chpl.complaint.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.complaint.domain.Complaint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintSearchResponse implements Serializable {
    private static final long serialVersionUID = 5130424476125198329L;
    private Integer recordCount;
    private Integer pageSize;
    private Integer pageNumber;
    //Decided to use the whole Complaint object here instead of a pared-down "complaint search result"
    //object because almost all of the Complaint data already had to be queried from the database
    //in order to apply all of the filtering options. The Complaint object is not that big (especially
    //compared to something like a listing details object, or even a change requst object) so it
    //seems reasonable to send it back to the UI one page at a time.
    private List<Complaint> results = new ArrayList<Complaint>();
}
