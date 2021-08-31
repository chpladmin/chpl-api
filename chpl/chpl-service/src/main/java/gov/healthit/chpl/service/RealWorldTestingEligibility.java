package gov.healthit.chpl.service;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RealWorldTestingEligibility {
    private RealWorldTestingEligiblityReason reason;
    private Optional<Integer> eligibilityYear;
}
