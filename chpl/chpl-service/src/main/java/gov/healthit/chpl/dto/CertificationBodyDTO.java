package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationBodyDTO implements Serializable {
    private static final long serialVersionUID = 6650934397742636530L;
    private Long id;
    private String acbCode;
    private String name;
    private String website;
    private AddressDTO address;
    private boolean retired;
    private Date retirementDate;

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

    @Override
    public boolean equals(Object anotherObject) {
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
