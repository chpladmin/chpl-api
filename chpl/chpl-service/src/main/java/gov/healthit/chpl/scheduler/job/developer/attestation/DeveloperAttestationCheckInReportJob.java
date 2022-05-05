package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeveloperAttestationCheckInReportJob implements Job {

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private AttestationManager attestationManager;

    @Autowired
    private ChangeRequestDAO changeRequestDAO;

    @Autowired
    private DeveloperAttestationReportDataCollection developerAttestationReportDataCollection;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting Developer Attestation Check-in Report job. *********");

        // We need to manually create a transaction in this case because of how AOP works. When a method is
        // annotated with @Transactional, the transaction wrapper is only added if the object's proxy is called.
        // The object's proxy is not called when the method is called from within this class. The object's proxy
        // is called when the method is public and is called from a different object.
        // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    getDevelopersWithAttestationChangeRequestsForMostRecentAttestationPeriod().entrySet().stream()
                            .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                            .forEach(entry -> {
                                ChangeRequest mostRecentCR = getMostRecentChangeRequest(entry.getValue());
                                LOGGER.info("Developer Id: {} - {}", entry.getValue(), mostRecentCR != null ? mostRecentCR.getCurrentStatus().getChangeRequestStatusType().getName() : "...");
                            });
                } catch (Exception e) {
                    LOGGER.catching(e);
                }
            }
        });
        LOGGER.info("********* Completed Developer Attestation Check-in Report job. *********");
    }

    private ChangeRequest getMostRecentChangeRequest(List<ChangeRequest> changeRequests) {
        if (changeRequests == null || changeRequests.size() == 0) {
            return null;
        } else if (changeRequests.size() == 1) {
            return changeRequests.get(0);
        } else {
            return changeRequests.stream()
                    .sorted((cr1, cr2) -> cr1.getSubmittedDate().compareTo(cr2.getSubmittedDate()))
                    .toList()
                    .get(0);
        }
    }

    private Map<Long, List<ChangeRequest>> getDevelopersWithAttestationChangeRequestsForMostRecentAttestationPeriod() throws EntityRetrievalException {
        Map<Long, List<ChangeRequest>> map = new HashMap<Long, List<ChangeRequest>>();
        List<Long> developerIds = getDeveloperIdsFromDeveloperAttestationReport();
        List<ChangeRequest> changeRequests = getAllAttestationChangeRequestsForMostRecentPastAttestationPeriod();

        for (Long developerId : developerIds) {
            map.put(developerId, changeRequests.stream()
                    .filter(cr -> cr.getDeveloper().getDeveloperId().equals(developerId))
                    .toList());
        }
        return map;
    }

    private List<ChangeRequest> getAllAttestationChangeRequestsForMostRecentPastAttestationPeriod() throws EntityRetrievalException {
        AttestationPeriod period = attestationManager.getMostRecentPastAttestationPeriod();
        return changeRequestDAO.getAll().stream()
                .filter(cr -> cr.getChangeRequestType().isAttestation()
                        && ((ChangeRequestAttestationSubmission) cr.getDetails()).getAttestationPeriod().equals(period))
                .toList();
    }

    private List<Long> getDeveloperIdsFromDeveloperAttestationReport() {
        return developerAttestationReportDataCollection.collect(certificationBodyDAO.findAll().stream().map(acb -> acb.getId()).toList()).stream()
                .map(developerAttestationReport -> developerAttestationReport.getDeveloperId())
                .toList();
    }
}
