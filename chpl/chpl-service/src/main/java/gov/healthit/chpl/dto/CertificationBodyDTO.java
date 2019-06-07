package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.CertificationBody;
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

    public CertificationBodyDTO(final CertificationBody domain) {
        BeanUtils.copyProperties(domain, this);
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

    @Override
    public boolean equals(final Object anotherObject) {
        if (anotherObject == null || !(anotherObject instanceof CertificationBodyDTO)) {
            return false;
        }

        CertificationBodyDTO anotherAcb = (CertificationBodyDTO) anotherObject;
        if (this.getId() != null && anotherAcb.getId() != null
                && this.getId().longValue() == anotherAcb.getId().longValue()) {
            return true;
        }

        if (!StringUtils.isEmpty(this.getName()) && !StringUtils.isEmpty(anotherAcb.getName())
                && this.getName().equalsIgnoreCase(anotherAcb.getName())) {
            return true;
        }

        if (!StringUtils.isEmpty(this.getAcbCode()) && !StringUtils.isEmpty(anotherAcb.getAcbCode())
                && this.getAcbCode().equalsIgnoreCase(anotherAcb.getAcbCode())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.getId() != null) {
            return this.getId().hashCode();
        }

        if (!StringUtils.isEmpty(this.getName())) {
            return this.getName().hashCode();
        }

        if (!StringUtils.isEmpty(this.getAcbCode())) {
            return this.getAcbCode().hashCode();
        }
        return -1;
    }
}
