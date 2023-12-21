package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.ParticipantAgeStatisticsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ParticipantAgeStatistics implements Serializable {
    private static final long serialVersionUID = -3740031503734395401L;
    private Long id;
    private Long ageCount;
    private Long testParticipantAgeId;
    private String ageRange;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public ParticipantAgeStatistics(final ParticipantAgeStatisticsDTO dto) {
        this.id = dto.getId();
        this.ageCount = dto.getAgeCount();
        this.testParticipantAgeId = dto.getTestParticipantAgeId();
        this.deleted = dto.getDeleted();
        this.lastModifiedDate = dto.getLastModifiedDate();
        this.lastModifiedUser = dto.getLastModifiedUser();
        this.creationDate = dto.getCreationDate();
    }
}
