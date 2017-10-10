package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.CertificationBodyDTO;

public class CertificationBody implements Serializable {
    private static final long serialVersionUID = 5328477887912042588L;
    private Long id;
    private String acbCode;
    private String name;
    private String website;
    private Address address;
    private boolean isDeleted;

    public CertificationBody() {
    }

    public CertificationBody(CertificationBodyDTO dto) {
        this.id = dto.getId();
        this.acbCode = dto.getAcbCode();
        this.name = dto.getName();
        this.website = dto.getWebsite();
        if (dto.getAddress() != null) {
            this.address = new Address(dto.getAddress());
        }
        this.isDeleted = dto.getDeleted();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(final String website) {
        this.website = website;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    public String getAcbCode() {
        return acbCode;
    }

    public void setAcbCode(final String acbCode) {
        this.acbCode = acbCode;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(final boolean deleted) {
        this.isDeleted = deleted;
    }

}
