package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.CertificationBodyEntity;

/**
 * Certification Body object.
 * @author kekey
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertificationBodyDTO implements Serializable {
    private static final long serialVersionUID = 6650934397742636530L;
    private Long id;
    private String acbCode;
    private String name;
    private String website;
    private AddressDTO address;
    private boolean retired;
    private Date retirementDate;

    /**
     * No-args constructor.
     */
    public CertificationBodyDTO() {
    }

    public CertificationBodyDTO(final CertificationBodyEntity entity) {
        this.id = entity.getId();
        this.acbCode = entity.getAcbCode();
        this.name = entity.getName();
        this.website = entity.getWebsite();
        this.retired = entity.getRetired();
        this.retirementDate = entity.getRetirementDate();
        if (entity.getAddress() != null) {
            this.address = new AddressDTO(entity.getAddress());
        }
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

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(final AddressDTO address) {
        this.address = address;
    }

    public String getAcbCode() {
        return acbCode;
    }

    public void setAcbCode(final String acbCode) {
        this.acbCode = acbCode;
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
        return "CertificationBodyDTO [id=" + id + ", acbCode=" + acbCode + ", name=" + name + ", website=" + website
                + ", address=" + address + ", retired=" + retired + ", retirementDate=" + retirementDate + "]";
    }

}
