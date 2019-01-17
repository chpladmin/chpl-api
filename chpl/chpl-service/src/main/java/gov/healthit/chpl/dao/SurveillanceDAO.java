package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformityDocument;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityDocumentationEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface SurveillanceDAO {
    Long insertSurveillance(Surveillance surv) throws UserPermissionRetrievalException;

    Long insertNonconformityDocument(Long nonconformityId, SurveillanceNonconformityDocument doc)
            throws EntityRetrievalException;

    Long updateSurveillance(Surveillance newSurv)
            throws EntityRetrievalException, UserPermissionRetrievalException;

    SurveillanceEntity getSurveillanceByCertifiedProductAndFriendlyId(Long certifiedProductId,
            String survFriendlyId);

    SurveillanceEntity getSurveillanceById(Long id) throws EntityRetrievalException;

    List<SurveillanceEntity> getSurveillanceByCertifiedProductId(Long id);

    SurveillanceNonconformityDocumentationEntity getDocumentById(Long documentId)
            throws EntityRetrievalException;

    void deleteSurveillance(Surveillance surv) throws EntityRetrievalException;

    void deleteNonconformityDocument(Long documentId) throws EntityRetrievalException;

    Long insertPendingSurveillance(Surveillance surv) throws UserPermissionRetrievalException;

    PendingSurveillanceEntity getPendingSurveillanceById(Long id) throws EntityRetrievalException;

    List<PendingSurveillanceEntity> getPendingSurveillanceByAcb(Long acbId);

    void deletePendingSurveillance(Surveillance surv) throws EntityRetrievalException;

    List<SurveillanceType> getAllSurveillanceTypes();

    SurveillanceType findSurveillanceType(String type);

    SurveillanceType findSurveillanceType(Long id);

    List<SurveillanceRequirementType> getAllSurveillanceRequirementTypes();

    SurveillanceRequirementType findSurveillanceRequirementType(String type);

    SurveillanceRequirementType findSurveillanceRequirementType(Long id);

    List<SurveillanceResultType> getAllSurveillanceResultTypes();

    SurveillanceResultType findSurveillanceResultType(String type);

    SurveillanceResultType findSurveillanceResultType(Long id);

    List<SurveillanceNonconformityStatus> getAllSurveillanceNonconformityStatusTypes();

    SurveillanceNonconformityStatus findSurveillanceNonconformityStatusType(String type);

    SurveillanceNonconformityStatus findSurveillanceNonconformityStatusType(Long id);

    List<SurveillanceEntity> getAllSurveillance();

    List<SurveillanceNonconformityEntity> getAllSurveillanceNonConformities();

    List<PendingSurveillanceEntity> getAllPendingSurveillance();
}
