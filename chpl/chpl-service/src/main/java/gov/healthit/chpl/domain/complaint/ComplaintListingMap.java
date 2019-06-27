package gov.healthit.chpl.domain.complaint;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.dto.ComplaintListingMapDTO;

public class ComplaintListingMap {
    private Long id;
    private Long complaintId;
    private Long listingId;
    private String chplProductNumber;

    public ComplaintListingMap() {

    }

    public ComplaintListingMap(ComplaintListingMapDTO dto) {
        BeanUtils.copyProperties(dto, this);
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(final Long complaintId) {
        this.complaintId = complaintId;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(final Long listingId) {
        this.listingId = listingId;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

}
