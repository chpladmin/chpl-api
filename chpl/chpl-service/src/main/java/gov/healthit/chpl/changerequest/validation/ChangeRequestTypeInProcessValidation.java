package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestTypeInProcessValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbAction;

    @Value("${changerequest.status.pendingdeveloperaction}")
    private Long pendingDeveloperAction;

    private ChangeRequestDAO crDAO;

    @Autowired
    public ChangeRequestTypeInProcessValidation(final ChangeRequestDAO crDAO) {
        this.crDAO = crDAO;
    }

    @Override
    public List<String> getErrorMessages(ChangeRequestValidationContext context) {
        List<String> errorMessages = new ArrayList<String>();

        try {
            List<ChangeRequest> crs = crDAO.getByDeveloper(context.getNewChangeRequest().getDeveloper().getDeveloperId())
                    .stream()
                    .filter(cr -> cr.getChangeRequestType().getId().equals(context.getNewChangeRequest().getChangeRequestType().getId()))
                    .filter(cr -> getInProcessStatuses().stream()
                            .anyMatch(status -> cr.getCurrentStatus().getChangeRequestStatusType().getId()
                                    .equals(status)))
                    .collect(Collectors.toList());
            if (crs.size() > 0) {
                errorMessages.add(getErrorMessage("changeRequest.inProcess"));
            }
        } catch (EntityRetrievalException e) {
            // Not sure what happened here, but we'll assume that another
            // validator catches it
            return errorMessages;
        }
        return errorMessages;
    }

    private List<Long> getInProcessStatuses() {
        return new ArrayList<Long>(Arrays.asList(pendingAcbAction, pendingDeveloperAction));
    }
}
