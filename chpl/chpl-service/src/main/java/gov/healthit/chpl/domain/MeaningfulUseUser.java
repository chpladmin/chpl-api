package gov.healthit.chpl.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Deprecated
public class MeaningfulUseUser implements Serializable {
    private static final long serialVersionUID = -4803363243075068608L;

    private Long id;
    private Long muuCount;
    private Long muuDate;

    public boolean matches(final PromotingInteroperabilityUser other) {
        boolean result = false;

        if (this.getId() != null && other.getId() != null
                && this.getId().longValue() == other.getId().longValue()) {
            result = true;
        }
        return result;
    }

}
