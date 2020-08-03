package gov.healthit.chpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SpecialProperties {

    private Environment env;

    @Autowired
    public SpecialProperties(Environment env) {
        this.env = env;
    }

    public Date getEffectiveRuleDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            return sdf.parse(env.getProperty("cures.ruleEffectiveDate"));
        } catch (ParseException e) {
            LOGGER.error("Could not determine value of 'cures.ruleEffectiveDate'.", e);
            return null;
        }
    }
}
