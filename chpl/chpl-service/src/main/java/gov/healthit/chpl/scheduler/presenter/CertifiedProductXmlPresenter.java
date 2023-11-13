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

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.scheduler.job.xmlgenerator.CertifiedProductSearchDetailsXmlGenerator;

/**
 * Present objects as XML file.
 * @author alarned
 *
 */
public class CertifiedProductXmlPresenter extends CertifiedProductPresenter implements AutoCloseable {
    private Logger logger;

    private Writer writer = null;
    private XMLStreamWriter streamWriter = null;

    @Override
    public void open(final File file) throws IOException {
        getLogger().info("Opening file, initializing XML doc.");
        try {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            streamWriter = factory.createXMLStreamWriter(writer);
            streamWriter.writeStartDocument("UTF-8", "1.0");
            streamWriter.writeStartElement("ns2:results");
            streamWriter.writeNamespace("ns2", "http://chpl.healthit.gov/listings");
            streamWriter.writeStartElement("listings");
            streamWriter.flush();
        } catch (Exception e) {
            throw new IOException("Error opening/initializing XML file.", e);
        }
    }

    @Override
    public synchronized void add(final CertifiedProductSearchDetails cp) throws IOException {
        try {
            getLogger().info("Adding CP to XML file: " + cp.getId());
            CertifiedProductSearchDetailsXmlGenerator.add(cp, "listing", streamWriter);
            streamWriter.flush();
        } catch (XMLStreamException e) {
            throw new IOException("Error adding listing to XML file. - " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        getLogger().info("Closing the XML file.");
        try {
            streamWriter.writeEndElement();
            streamWriter.writeEndElement();
            streamWriter.writeEndDocument();
            streamWriter.flush();
        } catch (Exception e) {
            getLogger().error(e);
        } finally {
            if (streamWriter != null) {
                try {
                    streamWriter.close();
                } catch (Exception e) {
                    throw new IOException("Error closing XMLStreamWriter", e);
                }
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
        }
        if (writer != null) {
            writer.close();
        }
    }

    public void setLogger(final Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = LogManager.getLogger(CertifiedProductXmlPresenter.class);
        }
        return logger;
    }
}
