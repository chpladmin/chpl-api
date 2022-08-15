package gov.healthit.chpl.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class QuestionAllowedResponseMap {
    private Long id;
    private Question question;
    private AllowedResponse response;
    private Integer sortOrder;
}
