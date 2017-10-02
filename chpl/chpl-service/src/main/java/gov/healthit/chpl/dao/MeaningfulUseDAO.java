package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.MeaningfulUseAccurateAsOfDTO;

public interface MeaningfulUseDAO {
    public MeaningfulUseAccurateAsOfDTO getMeaningfulUseAccurateAsOf();

    public MeaningfulUseAccurateAsOfDTO updateAccurateAsOf(MeaningfulUseAccurateAsOfDTO muuAccurateDTO);
}
