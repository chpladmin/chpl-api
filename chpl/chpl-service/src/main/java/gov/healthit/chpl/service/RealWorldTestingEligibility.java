package gov.healthit.chpl.service;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RealWorldTestingEligibility implements Serializable {
    private static final long serialVersionUID = -3841996659410634722L;

    private RealWorldTestingEligiblityReason reason;
    private Integer eligibilityYear;
}
