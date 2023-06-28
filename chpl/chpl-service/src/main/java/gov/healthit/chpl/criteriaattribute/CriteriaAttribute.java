package gov.healthit.chpl.criteriaattribute;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class CriteriaAttribute implements Serializable {
    private static final long serialVersionUID = 2856878300304895096L;

    private Long id;
    private String value;
    private String regulationTextCitation;
    private LocalDate startDay;
    private LocalDate endDay;
    private LocalDate requiredDay;
    private Rule rule;
}
