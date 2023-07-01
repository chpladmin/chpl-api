package gov.healthit.chpl.criteriaattribute;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CriteriaAttribute implements Serializable {
    private static final long serialVersionUID = 2856878300304895096L;

    private Long id;
    private String value;
    private String regulationTextCitation;
    private LocalDate startDay;
    private LocalDate endDay;
    private LocalDate requiredDay;
    private Rule rule;

    @JsonIgnore
    public Boolean isRetired() {
        LocalDate start = this.getStartDay() != null ? this.getStartDay() : LocalDate.MIN;
        LocalDate end = this.getEndDay() != null ? this.getEndDay() : LocalDate.MAX;
        return LocalDate.now().compareTo(start) >= 0 && LocalDate.now().compareTo(end) <= 0;
    }
}
