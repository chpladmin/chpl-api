package gov.healthit.chpl.realworldtesting.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RealWorldTestingUrlByDeveloper {
    private String url;
    private Long activeCertificateCount;
}
