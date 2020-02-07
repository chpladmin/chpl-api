package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
public @Data class CertificationBody implements Serializable {
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

    public CertificationBody(final CertificationBodyDTO dto) {
        this.id = dto.getId();
        this.acbCode = dto.getAcbCode();
        this.name = dto.getName();
        this.website = dto.getWebsite();
        this.retired = dto.isRetired();
        this.retirementDate = dto.getRetirementDate();
        if (dto.getAddress() != null) {
            this.address = new Address(dto.getAddress());
        }
    }

    public CertificationBody(final CertificationBodyEntity entity) {
        this.id = entity.getId();
        this.acbCode = entity.getAcbCode();
        this.name = entity.getName();
        this.website = entity.getWebsite();
        this.retired = entity.getRetired();
        this.retirementDate = entity.getRetirementDate();
        if (entity.getAddress() != null) {
            this.address = new Address(entity.getAddress());
        }
    }
}
