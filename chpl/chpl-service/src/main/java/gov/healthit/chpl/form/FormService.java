package gov.healthit.chpl.form;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.form.dao.FormDAO;
import gov.healthit.chpl.form.entity.FormEntity;

@Component
public class FormService {
    private FormDAO formDAO;

    @Autowired
    public FormService(FormDAO formDAO) {
        this.formDAO = formDAO;
    }

    @Transactional(readOnly = true)
    public Form getForm(Long formId) throws EntityRetrievalException {
        FormEntity formEntity = formDAO.getForm(formId);
        return Form.builder()
                .id(formEntity.getId())
                .description(formEntity.getDescription())
                .formItems(getFormItems(formEntity.getId(), null))
                .build();
    }

    private List<FormItem> getFormItems(Long formId, Long parentFormItemId) {
        List<FormItem> formItems = formDAO.getFormItems(formId, parentFormItemId).stream()
                .map(entity -> entity.toDomain())
                .toList();

        formItems.forEach(fi -> {
            fi.setChildFormItems(getFormItems(formId, fi.getId()));
        });
        return formItems;
    }
}
