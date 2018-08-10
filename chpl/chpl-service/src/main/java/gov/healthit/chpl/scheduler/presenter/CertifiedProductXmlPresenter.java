package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.scheduler.job.DownloadableResourceCreatorJob;
import gov.healthit.chpl.scheduler.job.xmlgenerator.CertifiedProductSearchDetailsXmlGenerator;

/**
 * Present objects as XML file.
 * @author alarned
 *
 */
public class CertifiedProductXmlPresenter implements CertifiedProductPresenter {
    private static final Logger LOGGER = LogManager.getLogger(DownloadableResourceCreatorJob.class);

    @Override
    public int presentAsFile(final File file, final CertifiedProductDownloadResponse cpList) {
        try {
            Writer writer = null;
            XMLStreamWriter streamWriter = null;
            try {
                XMLOutputFactory factory = XMLOutputFactory.newInstance();
                writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
                streamWriter = factory.createXMLStreamWriter(writer);
                //streamWriter = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(writer));
                streamWriter.writeStartDocument("UTF-8", "1.0");
                streamWriter.writeStartElement("ns2:results");
                streamWriter.writeNamespace("ns2", "http://chpl.healthit.gov/listings");
                CertifiedProductSearchDetailsXmlGenerator.add(cpList.getListings(), "listings", streamWriter);
                streamWriter.writeEndElement();
                streamWriter.writeEndDocument();
                                
            } catch(Exception e) {
                e.printStackTrace();
                LOGGER.error(e);
            } finally {
                if (streamWriter != null) {
                    streamWriter.close();
                }
                if (writer != null) {
                    writer.close();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while writing XML file.", e);
        }
        return cpList.getListings().size();
    }
}
