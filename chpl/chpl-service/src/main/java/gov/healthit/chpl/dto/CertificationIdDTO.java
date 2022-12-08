package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.CertificationIdEntity;
import gov.healthit.chpl.util.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CertificationIdDTO implements Serializable {
    private static final long serialVersionUID = 1338639051071192137L;
    private Long id;
    private String certificationId;
    private String year;
    private Long practiceTypeId;

    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public CertificationIdDTO(CertificationIdEntity entity) {

        this.id = entity.getId();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.certificationId = entity.getCertificationId();
        this.year = entity.getYear();
        this.practiceTypeId = entity.getPracticeTypeId();
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }
}
