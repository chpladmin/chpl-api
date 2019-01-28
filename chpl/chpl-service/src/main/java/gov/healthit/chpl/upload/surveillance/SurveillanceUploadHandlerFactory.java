package gov.healthit.chpl.upload.surveillance;

import java.util.List;

import org.apache.commons.csv.CSVRecord;

import gov.healthit.chpl.exception.InvalidArgumentsException;

public interface SurveillanceUploadHandlerFactory {
    SurveillanceUploadHandler getHandler(CSVRecord heading, List<CSVRecord> survRecords)
            throws InvalidArgumentsException;
}
