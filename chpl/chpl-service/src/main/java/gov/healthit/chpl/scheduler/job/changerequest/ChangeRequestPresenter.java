package gov.healthit.chpl.scheduler.job.changerequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.util.DateUtil;

public class ChangeRequestPresenter implements AutoCloseable {
    private Logger logger;
    private OutputStreamWriter writer = null;
    private CSVPrinter csvPrinter = null;

    public ChangeRequestPresenter(Logger logger) {
        this.logger = logger;
    }

    public void open(File file) throws IOException {
        logger.info("Opening file, initializing CSV doc.");
        writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        writer.write('\ufeff');
        csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
        csvPrinter.printRecord(generateHeaderValues());
        csvPrinter.flush();
    }

    public synchronized void add(ChangeRequest data) throws IOException {
        logger.info("Adding Change Request to CSV file: " + data.getId());
        List<String> rowValue = generateRowValue(data);
        if (rowValue != null) { // a subclass could return null to skip a row
            csvPrinter.printRecord(rowValue);
            csvPrinter.flush();
        }
    }

    public void close() throws IOException {
        logger.info("Closing the CSV file.");
        csvPrinter.close();
        writer.close();
    }

    protected List<String> generateHeaderValues() {
        //TODO: Can the "Custom Field" headers be improved upon? What about for demographic change requests?
        return Stream.of("Developer",
                "Request Type",
                "Creation Date",
                "Request Status",
                "Last Status Change",
                "Relevant ONC-ACBs",
                "Custom Field 1",
                "Custom Field 2",
                "Custom Field 3",
                "Custom Field 4",
                "Custom Field 5",
                "Custom Field 6",
                "Custom Field 7")
                .collect(Collectors.toList());
    }

    protected List<String> generateRowValue(ChangeRequest changeRequest) {
        List<String> result = new ArrayList<String>();
        result.add(changeRequest.getDeveloper().getName());
        result.add(changeRequest.getChangeRequestType().getName());
        result.add(DateUtil.formatInEasternTime(changeRequest.getSubmittedDate()));
        result.add(changeRequest.getCurrentStatus().getChangeRequestStatusType().getName());
        result.add(DateUtil.formatInEasternTime(changeRequest.getCurrentStatus().getStatusChangeDate()));
        result.add(changeRequest.getCertificationBodies().stream()
                .map(acb -> acb.getName())
                .collect(Collectors.joining(",")));
        ChangeRequestAttestationSubmission details = (ChangeRequestAttestationSubmission) changeRequest.getDetails();
        if (details.getAttestationResponses().size() > 0) {
            result.add(details.getAttestationResponses().get(0).getAttestation().getCondition().getName()
                + ": " + details.getAttestationResponses().get(0).getResponse().getResponse());
        }
        if (details.getAttestationResponses().size() > 1) {
            result.add(details.getAttestationResponses().get(1).getAttestation().getCondition().getName()
                + ": " + details.getAttestationResponses().get(1).getResponse().getResponse());
        }
        if (details.getAttestationResponses().size() > 2) {
            result.add(details.getAttestationResponses().get(2).getAttestation().getCondition().getName()
                + ": " + details.getAttestationResponses().get(2).getResponse().getResponse());
        }
        if (details.getAttestationResponses().size() > 3) {
            result.add(details.getAttestationResponses().get(3).getAttestation().getCondition().getName()
                + ": " + details.getAttestationResponses().get(3).getResponse().getResponse());
        }
        if (details.getAttestationResponses().size() > 4) {
            result.add(details.getAttestationResponses().get(4).getAttestation().getCondition().getName()
                + ": " + details.getAttestationResponses().get(4).getResponse().getResponse());
        }
        return result;
    }

}
