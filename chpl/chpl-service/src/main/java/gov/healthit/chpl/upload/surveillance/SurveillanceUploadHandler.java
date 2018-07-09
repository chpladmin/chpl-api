package gov.healthit.chpl.upload.surveillance;

import java.util.List;

import org.apache.commons.csv.CSVRecord;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.exception.InvalidArgumentsException;

public interface SurveillanceUploadHandler {
    public Surveillance handle() throws InvalidArgumentsException;

    public List<CSVRecord> getRecord();

    public void setRecord(final List<CSVRecord> record);

    public CSVRecord getHeading();

    public void setHeading(final CSVRecord heading);

    public int getLastDataIndex();

    public void setLastDataIndex(final int lastDataIndex);
}
