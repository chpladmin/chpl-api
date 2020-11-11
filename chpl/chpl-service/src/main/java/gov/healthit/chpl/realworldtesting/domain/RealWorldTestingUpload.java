package gov.healthit.chpl.realworldtesting.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RealWorldTestingUpload implements Serializable {
    private static final long serialVersionUID = -5933226973828290819L;

    private String chplProductNumber;
    private LocalDate lastChecked;
    private RealWorldTestingType type;
    private String url;
    private List<String> validationErrors = new ArrayList<String>();
    private Long order;
}
