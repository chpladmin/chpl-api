package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public class CertifiedProductJsonPresenter extends CertifiedProductPresenter implements AutoCloseable {
    private Logger logger;

    private JsonFactory jfactory = new JsonFactory();
    private ObjectMapper mapper = new ObjectMapper();
    private JsonGenerator jGenerator = null;

    @Override
    public void open(File file) throws IOException {
        getLogger().info("Opening file, initializing JSON doc.");

        jfactory.setCodec(mapper);
        jGenerator = jfactory.createGenerator(file, JsonEncoding.UTF8);
        jGenerator.writeStartArray();
    }

    @Override
    public synchronized void add(CertifiedProductSearchDetails cp) throws IOException {
        getLogger().info("Adding CP to JSON file: " + cp.getId());
        jGenerator.writeObject(cp);
    }

    @Override
    public void close() throws Exception {
        getLogger().info("Closing the JSON file.");
        jGenerator.writeEndArray();
        jGenerator.close();
    }

    public void setLogger(final Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = LogManager.getLogger(CertifiedProductJsonPresenter.class);
        }
        return logger;
    }

}
