package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.entity.statistics.IncumbentDevelopersStatisticsEntity;
import lombok.Data;

/**
 * Incumbent Developers data transfer object.
 * @author alarned
 *
 */
@Data
public class IncumbentDevelopersStatisticsDTO implements Serializable {

    private static final long serialVersionUID = -1536844909545189801L;

    private Long id;
    private Long newCount;
    private Long incumbentCount;
    private Long oldCertificationEditionId;
    private Long newCertificationEditionId;
    private CertificationEdition oldCertificationEdition;
    private CertificationEdition newCertificationEdition;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public IncumbentDevelopersStatisticsDTO() {
    }

    /**
     * Constructor that will populate the created object based on the entity
     * that is passed in as a parameter.
     * @param entity IncumbentDevelopersStatisticsEntity entity
     */
    public IncumbentDevelopersStatisticsDTO(final IncumbentDevelopersStatisticsEntity entity) {
        this.id = entity.getId();
        this.newCount = entity.getNewCount();
        this.incumbentCount = entity.getIncumbentCount();
        this.oldCertificationEditionId = entity.getOldCertificationEditionId();
        this.newCertificationEditionId = entity.getNewCertificationEditionId();
        if (entity.getOldCertificationEdition() != null) {
            this.oldCertificationEdition = entity.getOldCertificationEdition().toDomain();
        }
        if (entity.getNewCertificationEdition() != null) {
            this.newCertificationEdition = entity.getNewCertificationEdition().toDomain();
        }
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }
}
