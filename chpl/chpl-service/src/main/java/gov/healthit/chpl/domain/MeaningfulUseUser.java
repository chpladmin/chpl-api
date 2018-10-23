package gov.healthit.chpl.domain;

import java.io.Serializable;
import gov.healthit.chpl.dto.MeaningfulUseUserDTO;

/**
 * Domain-level container for meaningful use user counts.
 * @author kekey
 *
 */
public class MeaningfulUseUser implements Serializable {
    private static final long serialVersionUID = -4803363243075068608L;

    private Long id;
    private Long muuCount;
    private Long muuDate;

    /**
     * Default constructor.
     */
    public MeaningfulUseUser() {
    };

    /**
     * Constructor to create from DTO object.
     * @param dto
     */
    public MeaningfulUseUser(final MeaningfulUseUserDTO dto) {
        this.id = dto.getId();
        this.muuCount = dto.getMuuCount();
        this.muuDate = dto.getMuuDate().getTime();
    };

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

    public Long getMuuDate() {
        return muuDate;
    }

    public void setMuuDate(final Long muuDate) {
        this.muuDate = muuDate;
    }

    /**
     * Check to see if this muu matches another one.
     * @param other muu to check against
     * @return true if the IDs match
     */
    public boolean matches(final MeaningfulUseUser other) {
        boolean result = false;

        if (this.getId() != null && other.getId() != null
                && this.getId().longValue() == other.getId().longValue()) {
            result = true;
        }
        return result;
    }

}
