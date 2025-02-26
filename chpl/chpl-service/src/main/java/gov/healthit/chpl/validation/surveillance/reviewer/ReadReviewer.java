package gov.healthit.chpl.validation.surveillance.reviewer;

import gov.healthit.chpl.domain.surveillance.Surveillance;

public interface ReadReviewer {

    void review(Surveillance surveillance);
}
