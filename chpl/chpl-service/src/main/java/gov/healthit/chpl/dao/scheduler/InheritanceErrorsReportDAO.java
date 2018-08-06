package gov.healthit.chpl.dao.scheduler;

import java.util.List;

import gov.healthit.chpl.dto.scheduler.InheritanceErrorsReportDTO;
import gov.healthit.chpl.entity.scheduler.InheritanceErrorsReportEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Data access object for the inheritance_errors_report table.
 * @author alarned
 *
 */
public interface InheritanceErrorsReportDAO {
    /**
     * Retrieves all of the inheritance_errors_report records as a list InheritanceErrorsReportDTO objects.
     * @return List<InheritanceErrorsReportDTO> all records
     */
    List<InheritanceErrorsReportDTO> findAll();

    /**
     * Marks all inheritance_errors_report records as deleted.
     */
    void deleteAll();

    /**
     * Inserts the data in the InheritanceErrorsReportDTO into the inheritance_errors_report table.
     * @param dto InheritanceErrorsReportDTO object populated with the data to be inserted.
     * @return InheritanceErrorsReportEntity representing the data that was inserted
     * @throws EntityCreationException when the data could not be inserted
     * @throws EntityRetrievalException when the newly created record cannot be retrieved
     */
    InheritanceErrorsReportEntity create(InheritanceErrorsReportDTO dto)
            throws EntityCreationException, EntityRetrievalException;
}
