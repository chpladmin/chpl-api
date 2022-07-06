package gov.healthit.chpl.changerequest.domain.service.email;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;

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

    public List<UserDTO> getUsersForDeveloper(Long developerId) {
        return userDeveloperMapDAO.getByDeveloperId(developerId).stream()
                .map(userDeveloperMap -> userDeveloperMap.getUser())
                .collect(Collectors.<UserDTO> toList());
    }

    public String getApprovalBody(ChangeRequest cr) {
        if (cr.getCurrentStatus().getCertificationBody() != null) {
            return cr.getCurrentStatus().getCertificationBody().getName();
        } else if (cr.getCurrentStatus().getUserPermission().getId().equals(adminPermission)) {
            return "CHPL Admin";
        } else if (cr.getCurrentStatus().getUserPermission().getId().equals(oncPermission)) {
            return "ONC";
        } else {
            return "";
        }
    }

    public String toHtmlString(ChangeRequestAttestationSubmission attestationSubmission, ChplHtmlEmailBuilder htmlBuilder) {
        List<String> headings = Arrays.asList("Condition", "Attestation", "Response");

        /*
        List<List<String>> rows = attestationSubmission.getAttestationResponses().stream()
                .map(resp -> Arrays.asList(
                        resp.getAttestation().getCondition().getName(),
                        convertPsuedoMarkdownToHtmlLink(resp.getAttestation().getDescription()),
                        resp.getResponse().getResponse()))
                .collect(Collectors.toList());

        return htmlBuilder.getTableHtml(headings, rows, "");
        */
        return "Need to deternmine what the new email should look like.";
    }

    private String convertPsuedoMarkdownToHtmlLink(String toConvert) {
        String regex = "^(.*)\\[(.*)\\]\\((.*)\\)(.*)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(toConvert);
        String converted = "";

        if (matcher.find() && matcher.groupCount() == 4) {
            converted = matcher.group(1) + "<a href=" + matcher.group(3) + ">" + matcher.group(2) + "</a>" + matcher.group(4);
        } else {
          converted = toConvert;
        }
        return converted;
    }

    public abstract void send(ChangeRequest cr) throws EmailNotSentException ;

}
