package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.MeaningfulUseAccurateAsOfDTO;

public interface MeaningfulUseDAO {
    MeaningfulUseAccurateAsOfDTO getMeaningfulUseAccurateAsOf();

    MeaningfulUseAccurateAsOfDTO updateAccurateAsOf(MeaningfulUseAccurateAsOfDTO muuAccurateDTO);
}
