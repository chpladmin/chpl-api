package gov.healthit.chpl.datadog;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnDemandUrlRequest implements Serializable {
    private static final long serialVersionUID = -3009297190983937267L;

    private String url;
}
