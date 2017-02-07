package gov.healthit.chpl.manager;

import gov.healthit.chpl.dto.MeaningfulUseAccurateAsOfDTO;

public interface MeaningfulUseManager {
	public MeaningfulUseAccurateAsOfDTO getMeaningfulUseAccurateAsOf();
	public MeaningfulUseAccurateAsOfDTO updateMeaningfulUseAccurateAsOf(MeaningfulUseAccurateAsOfDTO meaningfulUseAccurateAsOfDTO);
}
