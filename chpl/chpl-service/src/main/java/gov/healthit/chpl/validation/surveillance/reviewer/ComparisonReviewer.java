package gov.healthit.chpl.validation.surveillance.reviewer;

import gov.healthit.chpl.domain.surveillance.Surveillance;

public interface ComparisonReviewer {

    void review(Surveillance existingSurveillance, Surveillance updatedSurveillance);
}
