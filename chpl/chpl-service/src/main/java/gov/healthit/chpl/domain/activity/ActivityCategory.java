package gov.healthit.chpl.domain.activity;

import java.io.Serializable;

/**
 * Categories of activity which describe what happened
 * in any given activity event. Activity can have multiple categories.
 * @author kekey
 *
 */
public enum ActivityCategory implements Serializable {

    /**
     * Catch-All. All listing activity metadata will have this category.
     */
    LISTING,

    /**
     * A listing was uploaded.
     */
    LISTING_UPLOAD,

    /**
     * The status of a listing was changed.
     */
    LISTING_STATUS_CHANGE,

    /**
     * Something about surveillance associated with a listing has changed.
     */
    SURVEILLANCE,

    /**
     * A developer was changed.
     */
    DEVELOPER;
}
