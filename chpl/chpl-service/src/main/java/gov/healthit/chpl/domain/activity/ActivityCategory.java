package gov.healthit.chpl.domain.activity;

import java.io.Serializable;

public enum ActivityCategory implements Serializable {

    CREATE,
    UPDATE,
    DELETE,

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
    DEVELOPER,

    /**
     * A product was changed.
     */
    PRODUCT,

    /**
     * A version was changed.
     */
    VERSION,

    /**
     * An ACB was changed.
     */
    CERTIFICATION_BODY,

    /**
     * An ATL was changed.
     */
    TESTING_LAB,

    /**
     * A user was changed.
     */
    USER_MAINTENANCE;
}
