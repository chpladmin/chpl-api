package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ff4j.FF4j;
import org.ff4j.core.Feature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.logging.Loggable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Feature Flag Controller.
 *
 */
@Api(value = "feature-flags")
@RestController
@RequestMapping("/feature-flags")
@Loggable
public class FeatureFlagController {

    private FF4j ff4j;

    @Autowired
    public FeatureFlagController(final FF4j ff4j) {
        this.ff4j = ff4j;
    }

    @ApiOperation(value = "List all feature flags.", notes = "")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<Flag> getFeatureFlags() {
        List<Flag> flags = new ArrayList<Flag>();
        for (Map.Entry<String, Feature> entry : ff4j.getFeatures().entrySet()) {
            Flag flag = new Flag();
            flag.setKey(entry.getKey());
            flag.setActive(ff4j.check(entry.getKey()));
            flag.setName(entry.getKey());
            flag.setDescription(entry.getValue().getDescription());
            flags.add(flag);
        }
        return flags;
    }

    private static class Flag {
        private String key;
        private Boolean active;
        private String name;
        private String description;

        @SuppressWarnings("unused")
        public String getKey() {
            return key;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        @SuppressWarnings("unused")
        public Boolean getActive() {
            return active;
        }

        public void setActive(final Boolean active) {
            this.active = active;
        }

        @SuppressWarnings("unused")
        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        @SuppressWarnings("unused")
        public String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }
    }
}
