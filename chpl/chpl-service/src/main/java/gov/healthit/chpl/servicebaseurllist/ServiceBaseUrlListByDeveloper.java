package gov.healthit.chpl.servicebaseurllist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ServiceBaseUrlListByDeveloper {
    private String url;
}
