package gov.healthit.chpl.email;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ChplEmailMessage implements Serializable {
    private static final long serialVersionUID = 3935859167555097284L;

    private List<String> recipients;
    private String subject = "";
    private String body = "";
    private List<File> fileAttachments = null;
}
