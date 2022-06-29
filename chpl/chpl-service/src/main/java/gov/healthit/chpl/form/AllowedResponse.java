package gov.healthit.chpl.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AllowedResponse {
    private Long id;
    private String response;
}
