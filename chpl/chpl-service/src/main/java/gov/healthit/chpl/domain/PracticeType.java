package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.PracticeTypeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PracticeType implements Serializable {
    private static final long serialVersionUID = 8826782928545744059L;

    private Long id;
    private Date creationDate;
    private Boolean deleted;
    private String description;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private String name;

    public PracticeType(PracticeTypeDTO dto) {
        this.id = dto.getId();
        this.creationDate = dto.getCreationDate();
        this.deleted = dto.getDeleted();
        this.description = dto.getDescription();
        this.lastModifiedDate = dto.getLastModifiedDate();
        this.lastModifiedUser = dto.getLastModifiedUser();
        this.name = dto.getName();
    }
}
