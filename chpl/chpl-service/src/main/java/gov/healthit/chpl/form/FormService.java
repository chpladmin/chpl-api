package gov.healthit.chpl.form;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                .sectionHeadings(organizeFormItemsIntoSectionHeadings(getFormItems(formEntity.getId(), null)))
                .build();
    }

    public FormItem getFormItem(Long formItemID) throws EntityRetrievalException {
        return formDAO.getFormItem(formItemID).toDomain();
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

    private List<SectionHeading> organizeFormItemsIntoSectionHeadings(List<FormItem> formItems) {
        //Creates a copy of the SectionHeading object (using *.toBuilder().build())
        Map<SectionHeading, List<FormItem>> sections = formItems.stream()
                .collect(Collectors.groupingBy(fi -> fi.getQuestion().getSectionHeading().toBuilder().build()));

        return sections.entrySet().stream()
                .map(entry -> {
                    entry.getKey().setFormItems(entry.getValue());
                    return entry.getKey();
                })
                .toList();
    }
}
