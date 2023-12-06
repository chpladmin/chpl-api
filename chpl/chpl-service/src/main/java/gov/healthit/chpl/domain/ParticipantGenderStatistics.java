package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.ParticipantGenderStatisticsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ParticipantGenderStatistics implements Serializable {
    private static final long serialVersionUID = -7580335667077396395L;
    private Long id;
    private Long maleCount;
    private Long femaleCount;
    private Long unknownCount;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public ParticipantGenderStatistics(final ParticipantGenderStatisticsDTO dto) {
        this.id = dto.getId();
        this.maleCount = dto.getMaleCount();
        this.femaleCount = dto.getFemaleCount();
        this.unknownCount = dto.getUnknownCount();
        this.deleted = dto.getDeleted();
        this.lastModifiedDate = dto.getLastModifiedDate();
        this.lastModifiedUser = dto.getLastModifiedUser();
        this.creationDate = dto.getCreationDate();
    }
}
