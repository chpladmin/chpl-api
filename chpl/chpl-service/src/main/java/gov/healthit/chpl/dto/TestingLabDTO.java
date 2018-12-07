package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.TestingLabEntity;

/**
 * Testing Lab object.
 * @author kekey
 *
 */
public class TestingLabDTO implements Serializable {
    private static final long serialVersionUID = 3772645398248735019L;
    private Long id;
    private String testingLabCode;
    private AddressDTO address;
    private String name;
    private String website;
    private String accredidationNumber;
    private boolean retired;

    /**
     * No-args constructor.
     */
    public TestingLabDTO() {
    }

    /**
     * Create a testing lab object from entity.
     * @param entity
     */
    public TestingLabDTO(TestingLabEntity entity) {
        this.id = entity.getId();
        this.testingLabCode = entity.getTestingLabCode();
        if (entity.getAddress() != null) {
            this.address = new AddressDTO(entity.getAddress());
        }
        this.name = entity.getName();
        this.website = entity.getWebsite();
        this.accredidationNumber = entity.getAccredidationNumber();
        this.retired = entity.getRetired();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(final AddressDTO address) {
        this.address = address;
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

    public String getTestingLabCode() {
        return testingLabCode;
    }

    public void setTestingLabCode(final String testingLabCode) {
        this.testingLabCode = testingLabCode;
    }

    public String getAccredidationNumber() {
        return accredidationNumber;
    }

    public void setAccredidationNumber(final String accredidationNumber) {
        this.accredidationNumber = accredidationNumber;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(final boolean retired) {
        this.retired = retired;
    }
}
