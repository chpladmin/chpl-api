package gov.healthit.chpl.form.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.form.entity.FormEntity;
import gov.healthit.chpl.form.entity.FormItemEntity;

@Repository
public class FormDAO extends BaseDAOImpl{
    public FormEntity getForm(Long formId) throws EntityRetrievalException {
        List<FormEntity> result = entityManager.createQuery(
                "SELECT f "
                + "FROM FormEntity f "
                + "WHERE (NOT f.deleted = true) "
                + "AND f.id = :formId ", FormEntity.class)
                .setParameter("formId", formId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Form not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate Form in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    public List<FormItemEntity> getFormItems(Long formId, Long parentFormItemId) {
        List<FormItemEntity> result = entityManager.createQuery(
                "SELECT DISTINCT fi "
                + "FROM FormItemEntity fi "
                + "JOIN FETCH fi.form f "
                + "JOIN FETCH fi.question q "
                + "JOIN FETCH q.allowedResponses ar "
                + "JOIN FETCH q.responseCardinalityType "
                + "LEFT JOIN FETCH q.sectionHeading "
                + "WHERE (NOT fi.deleted = true) "
                + "AND (NOT q.deleted = true ) "
                + "AND (NOT ar.deleted = true) "
                + "AND fi.form.id = :formId "
                + "AND ((:parentFormItemId is null and fi.parentFormItem is null) or fi.parentFormItem.id = :parentFormItemId)",
                        FormItemEntity.class)
                .setParameter("formId", formId)
                .setParameter("parentFormItemId", parentFormItemId)
                .getResultList();
        return result;
    }
}
