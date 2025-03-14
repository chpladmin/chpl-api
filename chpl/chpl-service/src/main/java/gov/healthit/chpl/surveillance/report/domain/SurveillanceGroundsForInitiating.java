package gov.healthit.chpl.surveillance.report.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveillanceGroundsForInitiating implements Serializable {
    private static final long serialVersionUID = 5781529212152752783L;

    private Long id;
    private String name;
}
