package gov.healthit.chpl.changerequest.dao;

import java.util.List;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;

public interface DeveloperCertificationBodyMapDAO {

    List<CertificationBody> getCertificationBodiesForDeveloper(final Long developerId);

    List<Developer> getDevelopersForCertificationBody(final Long certificationBodyId);
}
