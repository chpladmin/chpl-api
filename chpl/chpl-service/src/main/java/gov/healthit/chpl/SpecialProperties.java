package gov.healthit.chpl;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.ff4j.FF4j;
import org.ff4j.core.Feature;
import org.ff4j.strategy.time.ReleaseDateFlipStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SpecialProperties {

    private FF4j ff4j;
    private Environment env;

    @Autowired
    public SpecialProperties(FF4j ff4j, Environment env) {
        this.ff4j = ff4j;
        this.env = env;
    }

    public Date getEffectiveRuleDate() {
        if (ff4j.exist(FeatureList.EFFECTIVE_RULE_DATE)) {
            Feature erdFeature = ff4j.getFeature(FeatureList.EFFECTIVE_RULE_DATE);
            if (erdFeature.getFlippingStrategy() instanceof ReleaseDateFlipStrategy) {
                ReleaseDateFlipStrategy rdfs = (ReleaseDateFlipStrategy) erdFeature.getFlippingStrategy();
                // Since there is no getter (only setter) for release date, we'll need to use reflection to get the
                // value
                Date releaseDate = null;
                Field releaseDateField = ReflectionUtils.findField(ReleaseDateFlipStrategy.class, "releaseDate");
                ReflectionUtils.makeAccessible(releaseDateField);
                releaseDate = (Date) ReflectionUtils.getField(releaseDateField, rdfs);
                if (releaseDate != null) {
                    return releaseDate;
                }
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            return sdf.parse(env.getProperty("cures.ruleEffectiveDate"));
        } catch (ParseException e) {
            LOGGER.error("Could not determine value of 'cures.ruleEffectiveDate'.", e);
            return null;
        }
    }
}
