package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.scheduler.job.DownloadableResourceCreatorJob;
import gov.healthit.chpl.scheduler.job.xmlgenerator.CertifiedProductSearchDetailsXmlGenerator;

/**
 * Present objects as XML file.
 * @author alarned
 *
 */
public class CertifiedProductXmlPresenter implements CertifiedProductPresenter {
    private static final Logger LOGGER = LogManager.getLogger(DownloadableResourceCreatorJob.class);

    private Writer writer = null;
    private XMLStreamWriter streamWriter = null;
    
    @Override
    public int presentAsFile(final File file, final CertifiedProductDownloadResponse cpList) {
        try {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            streamWriter = factory.createXMLStreamWriter(writer);
            streamWriter.writeStartDocument("UTF-8", "1.0");
            streamWriter.writeStartElement("ns2:results");
            streamWriter.writeNamespace("ns2", "http://chpl.healthit.gov/listings");
            //CertifiedProductSearchDetailsXmlGenerator.add(cpList.getListings(), "listings", streamWriter);
            streamWriter.writeStartElement("listings");
            
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
        return 1;
    }

    public void open(final File file) {
        LOGGER.info("Opening file, initializing XML doc.");
        try {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            streamWriter = factory.createXMLStreamWriter(writer);
            streamWriter.writeStartDocument("UTF-8", "1.0");
            streamWriter.writeStartElement("ns2:results");
            streamWriter.writeNamespace("ns2", "http://chpl.healthit.gov/listings");
            //CertifiedProductSearchDetailsXmlGenerator.add(cpList.getListings(), "listings", streamWriter);
            streamWriter.writeStartElement("listings");
            
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
    }

    public void add(CertifiedProductSearchDetails cp) {
        try {
            LOGGER.info("Adding CP: " + cp.getId());
            CertifiedProductSearchDetailsXmlGenerator.add(cp, "listing", streamWriter);
        } catch (XMLStreamException e) {
           LOGGER.error(e);
        }
    }
    
    public void close() throws XMLStreamException, IOException {
        LOGGER.info("Closing the XML file.");
        try {
            streamWriter.writeEndElement();
            streamWriter.writeEndElement();
            streamWriter.writeEndDocument();
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            if (streamWriter != null) {
                streamWriter.close();
                streamWriter = null;
            }
            if (writer != null) {
                writer.close();
                writer = null;
            }
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        
        if (streamWriter != null) {
            streamWriter.close();
            streamWriter = null;
        }
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }
}
