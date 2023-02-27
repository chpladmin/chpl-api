package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.time.LocalDate;

public class AttestatationFormMetaData {
	private static final Long INFO_BLOCKING_CONDITION = 1L;
    private static final Long ASSURANCES_CONDITION = 7L;
    private static final Long ASSURANCES_CONDITION_ORIG = 2L;
    private static final Long COMMUNICATIONS_CONDITION = 3L;
    private static final Long API_CONDITION = 4L;
    private static final Long RWT_CONDITION = 5L;
    
    public static Long getInformationBlockingConditionId(Long attestionPeriodId) {
    	return INFO_BLOCKING_CONDITION;
    }
    
    public static Long getAssurancesConditionId(Long attestationPeriodId) {
    	//TODO This needs to be handled better...
    	if (LocalDate.now().isBefore(LocalDate.of(2023, 4, 1))) {
    		return ASSURANCES_CONDITION_ORIG;
    	} else {
    		return ASSURANCES_CONDITION;
    	}
    }
    
    public static Long getCommunicationConditionId(Long attestionPeriodId) {
    	return COMMUNICATIONS_CONDITION;
    }
    
    public static Long getApiConditionId(Long attestionPeriodId) {
    	return API_CONDITION;
    }
    
    public static Long getRwtConditionId(Long attestionPeriodId) {
    	return RWT_CONDITION;
    }
}
