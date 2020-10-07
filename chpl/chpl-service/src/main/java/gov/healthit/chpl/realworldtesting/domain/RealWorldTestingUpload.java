package gov.healthit.chpl.realworldtesting.domain;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class RealWorldTestingUpload {

    private String chplProductNumber;
    private LocalDate lastChecked;
    private RealWorldTestingType  type;
    private String url;
    private List<String> errors;
}
