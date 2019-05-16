package gov.healthit.chpl.manager;

import java.util.List;

import org.quartz.SchedulerException;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.impl.UpdateCertifiedBodyException;

public interface CertificationBodyManager {
    CertificationBodyDTO create(CertificationBodyDTO acb)
            throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException;

    CertificationBodyDTO update(CertificationBodyDTO acb) throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, SchedulerException, UpdateCertifiedBodyException, ValidationException;

    CertificationBodyDTO retire(CertificationBodyDTO acb) throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, SchedulerException, UpdateCertifiedBodyException, ValidationException;

    CertificationBodyDTO unretire(Long acbId) throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateCertifiedBodyException;

    List<CertificationBodyDTO> getAll();

    List<CertificationBodyDTO> getAllActive();

    CertificationBodyDTO getById(Long id) throws EntityRetrievalException;
}
