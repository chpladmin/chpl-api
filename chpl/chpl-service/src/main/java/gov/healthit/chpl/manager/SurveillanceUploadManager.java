package gov.healthit.chpl.manager;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.exception.ValidationException;

public interface SurveillanceUploadManager {
    static final String HEADING_CELL_INDICATOR = "RECORD_STATUS__C";
    static final String NEW_SURVEILLANCE_BEGIN_INDICATOR = "New";
    static final String UPDATE_SURVEILLANCE_BEGIN_INDICATOR = "Update";
    static final String SUBELEMENT_INDICATOR = "Subelement";
    
    public int countSurveillanceRecords(MultipartFile file) throws ValidationException;
    public int countSurveillanceRecords(String fileContents) throws ValidationException;
    public List<Surveillance> parseUploadFile(MultipartFile file) throws ValidationException;
    public List<String> checkUploadedSurveillanceOwnership(Surveillance pendingSurv);
}
