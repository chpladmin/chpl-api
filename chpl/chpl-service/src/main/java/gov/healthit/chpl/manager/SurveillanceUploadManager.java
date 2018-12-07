package gov.healthit.chpl.manager;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.exception.ValidationException;

public interface SurveillanceUploadManager {
    String HEADING_CELL_INDICATOR = "RECORD_STATUS__C";
    String NEW_SURVEILLANCE_BEGIN_INDICATOR = "New";
    String UPDATE_SURVEILLANCE_BEGIN_INDICATOR = "Update";
    String SUBELEMENT_INDICATOR = "Subelement";

    int countSurveillanceRecords(MultipartFile file) throws ValidationException;
    int countSurveillanceRecords(String fileContents) throws ValidationException;
    List<Surveillance> parseUploadFile(MultipartFile file) throws ValidationException;
    List<String> checkUploadedSurveillanceOwnership(Surveillance pendingSurv);
}
