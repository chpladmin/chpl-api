package gov.healthit.chpl.changerequest.domain.service.email;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.form.FormItem;
import gov.healthit.chpl.form.SectionHeading;
import gov.healthit.chpl.util.NullSafeEvaluator;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class ChangeRequestEmail {
    private UserDeveloperMapDAO userDeveloperMapDAO;

    @Value("${user.permission.onc}")
    private Long oncPermission;

    @Value("${user.permission.admin}")
    private Long adminPermission;


    @Autowired
    public ChangeRequestEmail(UserDeveloperMapDAO userDeveloperMapDAO) {
        this.userDeveloperMapDAO = userDeveloperMapDAO;
    }

    public abstract void send(ChangeRequest cr) throws EmailNotSentException;

    public List<UserDTO> getUsersForDeveloper(Long developerId) {
        return userDeveloperMapDAO.getByDeveloperId(developerId).stream()
                .map(userDeveloperMap -> userDeveloperMap.getUser())
                .toList();
    }

    public String getApprovalBody(ChangeRequest cr) {
        if (cr.getCurrentStatus().getCertificationBody() != null) {
            return cr.getCurrentStatus().getCertificationBody().getName();
        } else if (cr.getCurrentStatus().getUserPermission().getId().equals(oncPermission)
                || cr.getCurrentStatus().getUserPermission().getId().equals(adminPermission)) {
            return "ONC";
        } else {
            LOGGER.warn("Unexpected change request ACB or User Permission. Change Requst ID: " + cr.getId());
            return "an administrator";
        }
    }

    public String toHtmlString(ChangeRequestAttestationSubmission attestationSubmission, ChplHtmlEmailBuilder htmlBuilder) {
        List<String> headings = Arrays.asList("Condition", "Attestation", "Response");

        List<List<String>> rows = attestationSubmission.getForm().getSectionHeadings().stream()
                .sorted(Comparator.comparing(SectionHeading::getSortOrder))
                .map(sh -> Arrays.asList(
                        sh.getName(),
                        convertMarkdownToHtml(NullSafeEvaluator.eval(() -> sh.getFormItems().get(0).getQuestion().getQuestion(), "Unknown")),
                        NullSafeEvaluator.eval(() -> sh.getFormItems().get(0).getSubmittedResponses().get(0).getResponse(), "")
                        + getNoncompliantCapResponses(NullSafeEvaluator.eval(() -> sh.getFormItems().get(0), null))))
                .toList();

        return htmlBuilder.getTableHtml(headings, rows, "");
    }

    private String getNoncompliantCapResponses(FormItem formItem) {
        StringBuilder responses = new StringBuilder();
        if (formItem != null && formItem.getChildFormItems() != null && formItem.getChildFormItems().size() > 0) {
            FormItem childFormItem = formItem.getChildFormItems().get(0);
            responses.append("<ul>");
            responses.append(childFormItem.getSubmittedResponses().stream()
                    .sorted(Comparator.comparing(ar -> ar.getSortOrder() != null ? ar.getSortOrder() : 0))
                    .map(resp -> "<li>" + resp.getResponse() + "</li>")
                    .collect(Collectors.joining()));
            responses.append("</ul>");
        }
        return responses.toString();
    }

    private String convertMarkdownToHtml(String toConvert) {
        Parser parser  = Parser.builder().build();
        Node document = parser.parse(toConvert);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String converted = renderer.render(document);
        return converted;
    }
}