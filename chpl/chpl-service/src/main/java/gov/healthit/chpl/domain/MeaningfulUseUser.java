package gov.healthit.chpl.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeaningfulUseUser implements Serializable {
    private static final long serialVersionUID = -4803363243075068608L;

    private Long id;
    private Long muuCount;
    private Long muuDate;

    public boolean matches(final PromotingInteroperability other) {
        boolean result = false;

        if (this.getId() != null && other.getId() != null
                && this.getId().longValue() == other.getId().longValue()) {
            result = true;
        }
        return result;
    }

}
