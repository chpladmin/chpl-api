package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.ParticipantExperienceStatisticsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ParticipantExperienceStatistics implements Serializable {
    private static final long serialVersionUID = -761630337976327445L;

    private Long id;
    private Long participantCount;
    private Integer experienceMonths;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public ParticipantExperienceStatistics(final ParticipantExperienceStatisticsDTO dto) {
        this.id = dto.getId();
        this.participantCount = dto.getParticipantCount();
        this.experienceMonths = dto.getExperienceMonths();
        this.deleted = dto.getDeleted();
        this.lastModifiedDate = dto.getLastModifiedDate();
        this.lastModifiedUser = dto.getLastModifiedUser();
        this.creationDate = dto.getCreationDate();
    }
}
