package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class SurveillanceNonconformityDocument implements Serializable {
    private static final long serialVersionUID = -7456509117016763596L;

    /**
     * Document internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Document file name
     */
    @XmlElement(required = true)
    private String fileName;

    /**
     * Document file type (XML, PDF, etc)
     */
    @XmlElement(required = true)
    private String fileType;

    @XmlTransient
    private byte[] fileContents;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public byte[] getFileContents() {
        return fileContents;
    }

    public void setFileContents(byte[] fileContents) {
        this.fileContents = fileContents;
    }
}
