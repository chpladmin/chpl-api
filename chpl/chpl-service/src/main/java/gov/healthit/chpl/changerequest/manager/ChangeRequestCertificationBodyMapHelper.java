package gov.healthit.chpl.changerequest.manager;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.ChangeRequestCertificationBodyMapDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestCertificationBodyMap;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component
public class ChangeRequestCertificationBodyMapHelper {
    private ChangeRequestCertificationBodyMapDAO crCertificationBodyMapDAO;

    @Autowired
    public ChangeRequestCertificationBodyMapHelper(
            final ChangeRequestCertificationBodyMapDAO crCertificationBodyMapDAO) {
        this.crCertificationBodyMapDAO = crCertificationBodyMapDAO;
    }

    public List<CertificationBody> getCertificationBodiesByChangeRequestId(final Long changeRequestId) {
        return crCertificationBodyMapDAO.getByChangeRequestId(changeRequestId).stream()
                .map(result -> result.getCertificationBody())
                .collect(Collectors.<CertificationBody> toList());
    }

    public ChangeRequestCertificationBodyMap saveCertificationBody(ChangeRequest cr, CertificationBody acb) {
        ChangeRequestCertificationBodyMap map = new ChangeRequestCertificationBodyMap();
        map.setCertificationBody(acb);
        map.setChangeRequest(cr);
        try {
            return crCertificationBodyMapDAO.create(map);
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

}
