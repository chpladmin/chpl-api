package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

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
    private Date retirementDate;

    /**
     * No-args constructor.
     */
    public TestingLabDTO() {
    }

    public TestingLabDTO(final TestingLabEntity entity) {
        this.id = entity.getId();
        this.testingLabCode = entity.getTestingLabCode();
        if (entity.getAddress() != null) {
            this.address = new AddressDTO(entity.getAddress());
        }
        this.name = entity.getName();
        this.website = entity.getWebsite();
        this.accredidationNumber = entity.getAccredidationNumber();
        this.retired = entity.getRetired();
        this.retirementDate = entity.getRetirementDate();
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

    public Date getRetirementDate() {
        return retirementDate;
    }

    public void setRetirementDate(final Date retirementDate) {
        this.retirementDate = retirementDate;
    }

    @Override
    public String toString() {
        return "TestingLabDTO [id=" + id + ", testingLabCode=" + testingLabCode + ", address=" + address + ", name="
                + name + ", website=" + website + ", accredidationNumber=" + accredidationNumber + ", retired="
                + retired + ", retirementDate=" + retirementDate + "]";
    }

}
