package gov.healthit.chpl.service;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.FeatureList;

@Service
public class UrlFormatter {
    private static final String ONC_FORMAT = "https://www.healthit.gov";
    private static final String ASTP_FORMAT = "https://astp.hhs.gov";

    private FF4j ff4j;

    @Autowired
    public UrlFormatter(FF4j ff4j) {
        this.ff4j = ff4j;
    }

    public String format(String input) {
        if (input.contains(ONC_FORMAT) && ff4j.check(FeatureList.DOMAIN)) {
            input = input.replace(ONC_FORMAT, ASTP_FORMAT);
        } else if (input.contains(ASTP_FORMAT) && !ff4j.check(FeatureList.DOMAIN)) {
            input = input.replace(ASTP_FORMAT, ONC_FORMAT);
        }
        return input;
    }
}
