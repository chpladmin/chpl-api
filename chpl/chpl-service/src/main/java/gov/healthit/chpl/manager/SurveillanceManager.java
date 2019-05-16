package gov.healthit.chpl.manager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformityDocument;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.manager.impl.SurveillanceAuthorityAccessDeniedException;

public interface SurveillanceManager {
    File getAllSurveillanceDownloadFile() throws IOException;
    File getSurveillanceWithNonconformitiesDownloadFile() throws IOException;
    File getBasicReportDownloadFile() throws IOException;

    void validate(Surveillance surveillance);

    Long createSurveillance(Long abcId, Surveillance surv)
            throws UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException;

    Long addDocumentToNonconformity(Long acbId, Long nonconformityId, SurveillanceNonconformityDocument doc)
            throws EntityRetrievalException;

    void updateSurveillance(Long acbId, Surveillance surv) throws EntityRetrievalException,
    UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException;

    Surveillance getById(Long survId) throws EntityRetrievalException;

    List<Surveillance> getByCertifiedProduct(Long cpId);

    SurveillanceNonconformityDocument getDocumentById(Long docId, boolean getFileContents)
            throws EntityRetrievalException;

    void deleteSurveillance(Long acbId, Surveillance surv)
            throws EntityRetrievalException, SurveillanceAuthorityAccessDeniedException;

    void deleteNonconformityDocument(Long acbId, Long documentId) throws EntityRetrievalException;
}
