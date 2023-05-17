package gov.healthit.chpl.questionableactivity.domain;

import gov.healthit.chpl.domain.Product;
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
public class QuestionableActivityProduct extends QuestionableActivityBase {
    private Long productId;
    private Product product;

    public Class<?> getActivityObjectClass() {
        return Product.class;
    }
}
