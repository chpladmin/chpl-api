package gov.healthit.chpl.domain;

import java.io.Serializable;

public enum PendingCertifiedProductStatus implements Serializable {
	PENDING(),
	REJECTED(),
	ACTIVE();
}
