package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.MeaningfulUseAccurateAsOfDTO;

public interface MeaningfulUseDAO {
	public MeaningfulUseAccurateAsOfDTO getMeaningfulUseAccurateAsOf();
	public void updateAccurateAsOfDate(MeaningfulUseAccurateAsOfDTO muuAccurateDTO);
}
