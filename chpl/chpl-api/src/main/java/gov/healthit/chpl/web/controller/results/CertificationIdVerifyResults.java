package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Results object when verifying Certification IDs "en masse".
 */
public class CertificationIdVerifyResults implements Serializable {
    private static final long serialVersionUID = -3582436342627660622L;
    private List<VerifyResult> results = new ArrayList<VerifyResult>();

    /** Default constructor. */
    public CertificationIdVerifyResults() {
    }

    /**
     * Constructed from a map.
     * @param map the map
     */
    public CertificationIdVerifyResults(final Map<String, Boolean> map) {
        this.importMap(map);
    }

    public List<VerifyResult> getResults() {
        return this.results;
    }

    private void importMap(final Map<String, Boolean> map) {
        for (Map.Entry<String, Boolean> entry : map.entrySet()) {
            VerifyResult item = new VerifyResult(entry.getKey(), entry.getValue());
            this.results.add(item);
        }
    }

    /**
     * Single result inside Cert ID verification result.
     */
    public static class VerifyResult implements Serializable {
        private static final long serialVersionUID = -85566386396366634L;
        private String id;
        private boolean valid;

        /**
         * Constructor based on id and validity.
         * @param id the id
         * @param valid whether or not the CMS ID is valid
         */
        public VerifyResult(final String id, final Boolean valid) {
            this.id = id;
            this.valid = valid;
        }

        public String getId() {
            return this.id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public boolean getValid() {
            return this.valid;
        }

        public void setValid(final boolean valid) {
            this.valid = valid;
        }
    }
}
