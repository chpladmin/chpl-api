package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import lombok.Data;

@Data
public class CertificationIdResults implements Serializable {
    private static final long serialVersionUID = 4350936762994127624L;
    private List<Product> products;
    private String ehrCertificationId;
    private Map<String, Integer> metCounts;
    private Map<String, Integer> metPercentages;
    private ArrayList<String> missingAnd = new ArrayList<String>();
    private List<ArrayList<String>> missingOr = new ArrayList<ArrayList<String>>();
    private List<ArrayList<String>> missingCombo = new ArrayList<ArrayList<String>>();
    private List<TreeMap<String, ArrayList<String>>> missingXOr = new ArrayList<TreeMap<String, ArrayList<String>>>();
    private String year;
    private boolean isValid;

    @Data
    public static class Product implements Serializable {
        private static final long serialVersionUID = 1487852426085184818L;
        private String name;
        private Long productId;
        private String version;
        private String chplProductNumber;

        public Product(CertifiedProductDetailsDTO dto) {
            this.name = dto.getProduct().getName();
            this.productId = dto.getId();
            this.version = dto.getVersion().getVersion();
            this.chplProductNumber = dto.getChplProductNumber();
        }
    }
}
