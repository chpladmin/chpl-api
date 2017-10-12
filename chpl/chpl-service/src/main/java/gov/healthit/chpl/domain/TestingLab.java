package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.TestingLabDTO;

public class TestingLab implements Serializable {
    private static final long serialVersionUID = 7787353272569398682L;
    private Long id;
    private String atlCode;
    private String name;
    private String website;
    private String accredidationNumber;
    private Address address;
    private boolean isDeleted;

    public TestingLab() {
    }

    public TestingLab(TestingLabDTO dto) {
        this.id = dto.getId();
        this.atlCode = dto.getTestingLabCode();
        this.name = dto.getName();
        this.website = dto.getWebsite();
        this.accredidationNumber = dto.getAccredidationNumber();
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

    public String getAtlCode() {
        return atlCode;
    }

    public void setAtlCode(final String atlCode) {
        this.atlCode = atlCode;
    }

    public String getAccredidationNumber() {
        return accredidationNumber;
    }

    public void setAccredidationNumber(final String accredidationNumber) {
        this.accredidationNumber = accredidationNumber;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(final boolean deleted) {
        this.isDeleted = deleted;
    }
}
