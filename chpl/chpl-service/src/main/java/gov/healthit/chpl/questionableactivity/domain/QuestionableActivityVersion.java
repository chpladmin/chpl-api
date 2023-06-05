package gov.healthit.chpl.questionableactivity.domain;

import gov.healthit.chpl.dto.ProductVersionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class QuestionableActivityVersion extends QuestionableActivityBase {
    private Long versionId;
    private ProductVersionDTO version;

    public Class<?> getActivityObjectClass() {
        return ProductVersionDTO.class;
    }
}
