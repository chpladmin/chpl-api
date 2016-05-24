package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.List;

public class CorrectiveActionPlanResults {
	private List<CorrectiveActionPlanDetails> plans;
	
	public CorrectiveActionPlanResults() {
		plans = new ArrayList<CorrectiveActionPlanDetails>();
	}

	public List<CorrectiveActionPlanDetails> getPlans() {
		return plans;
	}

	public void setPlans(List<CorrectiveActionPlanDetails> plans) {
		this.plans = plans;
	}
}
