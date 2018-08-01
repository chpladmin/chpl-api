package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.FileWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.scheduler.job.DownloadableResourceCreatorJob;
import gov.healthit.chpl.scheduler.job.xmlgenerator.CertifiedProductSerchDetailsXmlGenerator;

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
            FileWriter fw = null;
            XMLStreamWriter writer = null;
            try {
                XMLOutputFactory factory = XMLOutputFactory.newInstance();
                fw = new FileWriter(file);
                writer = factory.createXMLStreamWriter(fw);
                writer.writeStartDocument();
                CertifiedProductSerchDetailsXmlGenerator.add(cpList.getListings(), "listings", writer);
                writer.writeEndDocument();
                                
            } catch(Exception e) {
                e.printStackTrace();
                LOGGER.error(e);
            } finally {
                if (writer != null) {
                    writer.close();
                }
                if (fw != null) {
                    fw.close();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while writing XML file.", e);
        }
        return cpList.getListings().size();
    }
}
