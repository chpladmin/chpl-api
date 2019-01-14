package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.MeaningfulUseUserEntity;
import gov.healthit.chpl.util.Util;

/**
 * Business-layer DTO object for meaningful use users.
 * @author kekey
 *
 */
public class MeaningfulUseUserDTO implements Serializable {
    private static final long serialVersionUID = 2215818722558786140L;

    private Long id;
    private Long certifiedProductId;
    private Long muuCount;
    private Date muuDate;

    /**
     * Default constructor.
     */
    public MeaningfulUseUserDTO() {
    };

    /**
     * Constructor to create from entity.
     * @param entity
     */
    public MeaningfulUseUserDTO(final MeaningfulUseUserEntity entity) {
        this.id = entity.getId();
        this.certifiedProductId = entity.getCertifiedProductId();
        this.muuCount = entity.getMuuCount();
        this.muuDate = entity.getMuuDate();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getMuuCount() {
        return muuCount;
    }

    public void setMuuCount(final Long muuCount) {
        this.muuCount = muuCount;
    }

    public Date getMuuDate() {
        return Util.getNewDate(muuDate);
    }

    public void setMuuDate(final Date muuDate) {
        this.muuDate = Util.getNewDate(muuDate);
    }

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    };

}
