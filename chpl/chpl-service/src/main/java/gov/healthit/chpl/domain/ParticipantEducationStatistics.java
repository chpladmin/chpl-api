package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.ParticipantEducationStatisticsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ParticipantEducationStatistics implements Serializable {
    private static final long serialVersionUID = -1648513956784683632L;

    private Long id;
    private Long educationCount;
    private Long educationTypeId;
    private String education;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public ParticipantEducationStatistics(final ParticipantEducationStatisticsDTO dto) {
        this.id = dto.getId();
        this.educationCount = dto.getEducationCount();
        this.educationTypeId = dto.getEducationTypeId();
        this.deleted = dto.getDeleted();
        this.lastModifiedDate = dto.getLastModifiedDate();
        this.lastModifiedUser = dto.getLastModifiedUser();
        this.creationDate = dto.getCreationDate();
    }
}
