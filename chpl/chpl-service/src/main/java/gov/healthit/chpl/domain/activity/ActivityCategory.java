package gov.healthit.chpl.domain.activity;

import java.io.Serializable;

/**
 * Categories of activity which describe what happened
 * in any given activity event. Activity can have multiple categories.
 * @author kekey
 *
 */
public enum ActivityCategory implements Serializable {

    LISTING_UPLOAD,
    LISTING_STATUS_CHANGE,
    SURVEILLANCE;
}
