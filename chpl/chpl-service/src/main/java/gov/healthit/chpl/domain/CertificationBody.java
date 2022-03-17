package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class CertificationBody implements Serializable {
    private static final long serialVersionUID = 5328477887912042588L;
    private Long id;
    private String acbCode;
    private String name;
    private String website;
    private Address address;
    private boolean retired;
    private Date retirementDate;

    /**
     * No-args constructor.
     */
    public CertificationBody() {
    }

    public CertificationBody(CertificationBodyDTO dto) {
        this.id = dto.getId();
        this.acbCode = dto.getAcbCode();
        this.name = dto.getName();
        this.website = dto.getWebsite();
        this.retired = dto.isRetired();
        this.retirementDate = dto.getRetirementDate();
        this.address = dto.getAddress();
    }

    public CertificationBody(final CertificationBodyEntity entity) {
        this.id = entity.getId();
        this.acbCode = entity.getAcbCode();
        this.name = entity.getName();
        this.website = entity.getWebsite();
        this.retired = entity.getRetired();
        this.retirementDate = entity.getRetirementDate();
        if (entity.getAddress() != null) {
            this.address = entity.getAddress().toDomain();
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
        return "CertificationBody [id=" + id + ", acbCode=" + acbCode + ", name=" + name + ", website=" + website
                + ", address=" + address + ", retired=" + retired + ", retirementDate=" + retirementDate + "]";
    }

}
