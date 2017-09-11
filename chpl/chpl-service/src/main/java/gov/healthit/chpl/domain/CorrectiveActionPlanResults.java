package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CorrectiveActionPlanResults implements Serializable {
	private static final long serialVersionUID = 8597873999181093677L;
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
