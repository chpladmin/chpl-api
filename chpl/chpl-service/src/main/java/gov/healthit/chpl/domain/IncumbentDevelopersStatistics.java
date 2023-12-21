package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.IncumbentDevelopersStatisticsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class IncumbentDevelopersStatistics implements Serializable {
    private static final long serialVersionUID = -1648513956784683632L;

    private Long id;
    private Long newCount;
    private Long incumbentCount;
    private CertificationEdition oldCertificationEdition;
    private CertificationEdition newCertificationEdition;

    public IncumbentDevelopersStatistics(final IncumbentDevelopersStatisticsDTO dto) {
        this.id = dto.getId();
        this.setNewCount(dto.getNewCount());
        this.setIncumbentCount(dto.getIncumbentCount());
        if (dto.getOldCertificationEdition() != null) {
            this.oldCertificationEdition = dto.getOldCertificationEdition();
        }
        if (dto.getNewCertificationEdition() != null) {
            this.newCertificationEdition = dto.getNewCertificationEdition();
        }

    }

    @Override
    public String toString() {
        return "Incumbent Developers Statistics Domain object ["
                + "[New: " + this.newCount + "]"
                + "[Incumbent: " + this.incumbentCount + "]"
                + "[Old Edition: " + this.oldCertificationEdition.getName() + "]"
                + "[New Edition: " + this.newCertificationEdition.getName() + "]"
                + "]";
    }
}
