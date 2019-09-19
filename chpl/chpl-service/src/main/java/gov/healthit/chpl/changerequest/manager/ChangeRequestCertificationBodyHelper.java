package gov.healthit.chpl.changerequest.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component
public class ChangeRequestCertificationBodyHelper {
    private CertifiedProductDAO certifiedProductDAO;
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    public ChangeRequestCertificationBodyHelper(final CertifiedProductDAO certifiedProductDAO,
            final CertificationBodyDAO certificationBodyDAO) {
        this.certifiedProductDAO = certifiedProductDAO;
        this.certificationBodyDAO = certificationBodyDAO;
    }

    public List<CertificationBody> getCertificationBodiesByDeveloper(final Developer developer) {
        Map<Long, CertificationBody> acbs = new HashMap<Long, CertificationBody>();

        certifiedProductDAO.findByDeveloperId(developer.getDeveloperId()).stream()
                .filter(result -> !acbs.containsKey(result.getCertificationBodyId()))
                .forEach(result -> {
                    acbs.put(result.getCertificationBodyId(), getCertificationBody(result.getCertificationBodyId()));
                });
        return new ArrayList<CertificationBody>(acbs.values());
    }

    private CertificationBody getCertificationBody(final Long certificationBodyId) {
        try {
            return new CertificationBody(certificationBodyDAO.getById(certificationBodyId));
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

}
