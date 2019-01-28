package gov.healthit.chpl.upload.surveillance;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.exception.InvalidArgumentsException;

@Service
public final class SurveillanceUploadHandlerFactoryImpl implements SurveillanceUploadHandlerFactory {
    public static final int NUM_FIELDS_2015 = 23;

    @Autowired
    private SurveillanceUploadHandler handler2015;

    private SurveillanceUploadHandlerFactoryImpl() {
    }

    @Override
    public SurveillanceUploadHandler getHandler(final CSVRecord heading, final List<CSVRecord> survRecords)
            throws InvalidArgumentsException {
        SurveillanceUploadHandler handler = null;

        int lastDataIndex = -1;
        for (int i = 0; i < heading.size() && lastDataIndex < 0; i++) {
            String headingValue = heading.get(i);
            if (StringUtils.isEmpty(headingValue)) {
                lastDataIndex = i - 1;
            } else if (i == heading.size() - 1) {
                lastDataIndex = i;
            }
        }

        if ((lastDataIndex + 1) == NUM_FIELDS_2015) {
            handler = handler2015;
        } else {
            throw new InvalidArgumentsException(
                    "Expected " + NUM_FIELDS_2015 + " fields in the record but found " + (lastDataIndex + 1));
        }

        handler.setRecord(survRecords);
        handler.setHeading(heading);
        handler.setLastDataIndex(lastDataIndex);
        return handler;
    }
}
