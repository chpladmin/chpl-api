package gov.healthit.chpl.realworldtesting.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealWorldTestingUploadResponse {
    private String email;
    private String fileName;
}
