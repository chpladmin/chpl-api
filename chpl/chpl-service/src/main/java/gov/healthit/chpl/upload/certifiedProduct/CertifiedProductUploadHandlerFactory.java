package gov.healthit.chpl.upload.certifiedProduct;

import java.util.List;

import org.apache.commons.csv.CSVRecord;

import gov.healthit.chpl.exception.InvalidArgumentsException;

public interface CertifiedProductUploadHandlerFactory {
    public CertifiedProductUploadHandler getHandler(CSVRecord heading, List<CSVRecord> cpRecords)
            throws InvalidArgumentsException;
}
