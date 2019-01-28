package gov.healthit.chpl.upload.surveillance;

import java.util.List;

import org.apache.commons.csv.CSVRecord;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.exception.InvalidArgumentsException;

public interface SurveillanceUploadHandler {
    Surveillance handle() throws InvalidArgumentsException;

    List<CSVRecord> getRecord();

    void setRecord(List<CSVRecord> record);

    CSVRecord getHeading();

    void setHeading(CSVRecord heading);

    int getLastDataIndex();

    void setLastDataIndex(int lastDataIndex);
}
