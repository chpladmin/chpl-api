package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
public class SedParticipantStatisticsCount implements Serializable {
    private static final long serialVersionUID = 6166234350175390349L;

    private Long id;
    private Long sedCount;
    private Long participantCount;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public SedParticipantStatisticsCount(final SedParticipantStatisticsCountDTO dto) {
        this.id = dto.getId();
        this.sedCount = dto.getSedCount();
        this.participantCount = dto.getParticipantCount();
        this.deleted = dto.getDeleted();
        this.lastModifiedDate = dto.getLastModifiedDate();
        this.lastModifiedUser = dto.getLastModifiedUser();
        this.creationDate = dto.getCreationDate();
    }
}
