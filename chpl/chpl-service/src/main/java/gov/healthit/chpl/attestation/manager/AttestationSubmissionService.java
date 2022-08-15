package gov.healthit.chpl.attestation.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.attestation.entity.AttestationSubmissionResponseEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.form.FormItem;
import gov.healthit.chpl.form.FormService;
import gov.healthit.chpl.form.SectionHeading;

@Component
public class AttestationSubmissionService {
    private AttestationDAO attestationDAO;
    private FormService formService;

    @Autowired
    public AttestationSubmissionService(AttestationDAO attestationDAO, FormService formService) {
        this.attestationDAO = attestationDAO;
        this.formService = formService;
    }

    public List<AttestationSubmission> getAttestationSubmissions(Long developerId) {
        return attestationDAO.getAttestationSubmissionsByDeveloper(developerId).stream()
                .map(sub -> {
                    sub.setForm(getPopulatedForm(sub));
                    return sub;
                })
                .toList();
    }

    private Form getPopulatedForm(AttestationSubmission submission) {
        try {
            List<AttestationSubmissionResponseEntity> submittedResponses = attestationDAO.getAttestationSubmissionResponseEntities(submission.getId());

            Form form = formService.getForm(submission.getAttestationPeriod().getForm().getId());
            for (SectionHeading heading : form.getSectionHeadings()) {
                heading.setFormItems(populateFormItemsWithSubmittedResponses(heading.getFormItems(), submittedResponses));
            }

            return form;
        } catch (EntityRetrievalException e) {
            return null;
        }
    }

    private List<FormItem> populateFormItemsWithSubmittedResponses(List<FormItem> formItems, List<AttestationSubmissionResponseEntity> submittedResponses) {
        for (FormItem fi : formItems) {
            fi.setSubmittedResponses(submittedResponses.stream()
                    .filter(sr -> sr.getFormItem().getId().equals(fi.getId()))
                    .map(sr -> sr.getResponse().toDomain())
                    .toList());

            fi.setChildFormItems(populateFormItemsWithSubmittedResponses(fi.getChildFormItems(), submittedResponses));
        }
        return formItems;
    }
}
