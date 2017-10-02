package gov.healthit.chpl.manager;

import gov.healthit.chpl.dto.MeaningfulUseAccurateAsOfDTO;

public interface MeaningfulUseManager {
    MeaningfulUseAccurateAsOfDTO getMeaningfulUseAccurateAsOf();

    MeaningfulUseAccurateAsOfDTO updateMeaningfulUseAccurateAsOf(
            MeaningfulUseAccurateAsOfDTO meaningfulUseAccurateAsOfDTO);
}
