package gov.healthit.chpl.certificationId;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CertificationIdVerificationBody {

    private List<String> certificationIds;
}
