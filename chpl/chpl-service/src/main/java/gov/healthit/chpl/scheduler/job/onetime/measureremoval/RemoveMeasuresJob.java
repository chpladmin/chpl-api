package gov.healthit.chpl.scheduler.job.onetime.measureremoval;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.listing.measure.MeasureDAO;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.CacheManager;

//@Log4j2(topic = "removeMeasuresJobLogger")
@Log4j2
public class RemoveMeasuresJob extends QuartzJob {

    @Autowired
    private MeasureDAO measureDAO;

    //Format: domain|measure
    private static final String[] MEASURES_TO_REMOVE = {
            "EC ACI Transition|Electronic Prescribing: Eligible Clinician",
            "EH/CAH Stage 2|Electronic Prescribing: Eligible Hospital/Critical Access Hospital",
            "EP Stage 2|Electronic Prescribing: Eligible Professional",
            "EC|Electronic Prescribing: Eligible Clinician",
            "EH/CAH Medicare and Medicaid PI|Electronic Prescribing: Eligible Hospital/Critical Access Hospital",
            "EP Medicaid PI|Electronic Prescribing: Eligible Professional",
            "EC|Query of Prescription Drug Monitoring Program (PDMP): Eligible Clinician",
            "EH/CAH Stage 3|Query of Prescription Drug Monitoring Program (PDMP): Eligible Hospital/Critical Access Hospital",
            "EC|Verify Opioid Treatment Agreement: Eligible Clinician",
            "EH/CAH Medicare PI|Verify Opioid Treatment Agreement: Eligible Hospital/Critical Access Hospital",
            "EC ACI|Patient-Specific Education: Eligible Clinician",
            "EC ACI Transition|Patient-Specific Education: Eligible Clinician",
            "EH/CAH Medicaid PI|Patient-Specific Education: Eligible Hospital/Critical Access Hospital",
            "EH/CAH Stage 2|Patient-Specific Education: Eligible Hospital/Critical Access Hospital",
            "EP Medicaid PI|Patient-Specific Education: Eligible Professional",
            "EP Stage 2|Patient-Specific Education: Eligible Professional",
            "EC ACI|Secure Electronic Messaging: Eligible Clinician",
            "EC ACI Transition|Secure Electronic Messaging: Eligible Clinician",
            "EH/CAH Medicaid PI|Secure Electronic Messaging: Eligible Hospital/Critical Access Hospital",
            "EH/CAH Stage 2|Secure Electronic Messaging: Eligible Hospital/Critical Access Hospital",
            "EP Medicaid PI|Secure Electronic Messaging: Eligible Professional",
            "EP Stage 2|Secure Electronic Messaging: Eligible Professional"};

    public RemoveMeasuresJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Remove Criteria job. *********");

        try {
        getMeasuresToRemove().stream()
                .forEach(measure -> {
                    Measure m = removeMeasure(measure);
                    LOGGER.always().log(String.format("%s    |     %s     |     %s     -- Has been removed", m.getId(), m.getDomain().getName(), m.getName()));
                });

        } catch (final Exception ex) {
            LOGGER.error("Exception updating measures.", ex);
        }

        CacheManager.getInstance().clearAll();
        LOGGER.info("********* Completed the Remove Criteria job. *********");
    }

    private Measure removeMeasure(Measure measure) {
        measure.setRemoved(true);
        return measureDAO.update(measure);
    }

    private List<Measure> getMeasuresToRemove() {
        Set<Measure> allMeasures = measureDAO.findAll();
        return Stream.of(MEASURES_TO_REMOVE)
                .map(str -> getMeasureFromString(str, allMeasures))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<Measure> getMeasureFromString(String measureString, Set<Measure> measures) {
        String[] measureParts = measureString.split("|");
        return measures.stream()
                .filter(m -> m.getDomain().getName().equals(measureParts[0])
                        && m.getName().equals(measureParts[1]))
                .findAny();
    }
}

