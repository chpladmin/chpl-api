package gov.healthit.chpl.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.impl.AddressDAOImpl;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ListingActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;

public class ListingActivityMetadataBuilder extends ActivityMetadataBuilder {
    private static final Logger LOGGER = LogManager.getLogger(ListingActivityMetadataBuilder.class);
    private ObjectMapper jsonMapper;

    public ListingActivityMetadataBuilder() {
        super();
        jsonMapper = new ObjectMapper();
    }

    protected void addConceptSpecificMetadata(final ActivityDTO dto, final ActivityMetadata metadata) {
        if (!(metadata instanceof ListingActivityMetadata)) {
            return;
        }
        ListingActivityMetadata listingMetadata = (ListingActivityMetadata) metadata;

        //parse listing specific metadata
        CertifiedProductSearchDetails origListing = null;
        if (dto.getOriginalData() != null) {
            LOGGER.debug("Activity ID " + dto.getId() + " origData not null. Parsing as CertifiedProductSearchDetails.");
            try {
                origListing =
                    jsonMapper.readValue(dto.getOriginalData(), CertifiedProductSearchDetails.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity ID " + dto.getId() + " original data. "
                        + "JSON was: " + dto.getOriginalData(), ex);
            }
        }

        CertifiedProductSearchDetails newListing = null;
        if (dto.getNewData() != null) {
            LOGGER.debug("Activity ID " + dto.getId() + " newData not null. Parsing as CertifiedProductSearchDetails.");
            try {
                newListing =
                    jsonMapper.readValue(dto.getNewData(), CertifiedProductSearchDetails.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity ID " + dto.getId() + " new data. "
                        + "JSON was: " + dto.getNewData(), ex);
            }
        }

        if (newListing != null) {
            //for listing activity newListing should really never be null since listings can't be deleted
            parseListingMetadata(listingMetadata, newListing);
        } else if (origListing != null) {
            //adding this here just in case in some future circumstance the newListing could have been null
            parseListingMetadata(listingMetadata, origListing);
        }

        categorizeActivity(listingMetadata, origListing, newListing);
    }

    private void parseListingMetadata(
            final ListingActivityMetadata listingMetadata, final CertifiedProductSearchDetails listing) {
        listingMetadata.setChplProductNumber(listing.getChplProductNumber());
        if (listing.getCertifyingBody() != null && listing.getCertifyingBody().get("name") != null) {
            listingMetadata.setAbcName(listing.getCertifyingBody().get("name").toString());
        }
        //there is at least one activity record that has a null certification date field
        //due to a bug in the system at the time of the activity
        listingMetadata.setCertificationDate(listing.getCertificationDate());
        if (listing.getDeveloper() != null) {
            listingMetadata.setDeveloperName(listing.getDeveloper().getName());
        }
        if (listing.getCertificationEdition() != null && listing.getCertificationEdition().get("name") != null) {
            listingMetadata.setEdition(listing.getCertificationEdition().get("name").toString());
        }
        if (listing.getProduct() != null) {
            listingMetadata.setProductName(listing.getProduct().getName());
        }
    }

    private void categorizeActivity(final ListingActivityMetadata listingMetadata,
            final CertifiedProductSearchDetails origListing, final CertifiedProductSearchDetails newListing) {
        if (origListing == null && newListing != null) {
            listingMetadata.getCategories().add(ActivityCategory.LISTING_UPLOAD);
        } else if (origListing != null && newListing != null) {
            //status change?
            if (origListing.getCertificationStatus() != null && newListing.getCertificationStatus() != null
                    && origListing.getCertificationStatus().getId() != newListing.getCertificationStatus().getId()) {
                listingMetadata.getCategories().add(ActivityCategory.LISTING_STATUS_CHANGE);
            } else if(origListing.getCurrentStatus() != null && newListing.getCurrentStatus() != null
                    && origListing.getCurrentStatus().getStatus() != null
                    && newListing.getCurrentStatus().getStatus() != null
                    && !origListing.getCurrentStatus().getStatus().getName().equals(newListing.getCurrentStatus().getStatus().getName())) {
                listingMetadata.getCategories().add(ActivityCategory.LISTING_STATUS_CHANGE);
            }
            //surveillance change?
            //check for surveillance added or removed
            if( (origListing.getSurveillance() != null && newListing.getSurveillance() == null)
                    || (origListing.getSurveillance() == null && newListing.getSurveillance() != null)) {
                listingMetadata.getCategories().add(ActivityCategory.SURVEILLANCE);
            } else if(origListing.getSurveillance() != null && newListing.getSurveillance() != null) {
                if(origListing.getSurveillance().size() != newListing.getSurveillance().size()) {
                    listingMetadata.getCategories().add(ActivityCategory.SURVEILLANCE);
                }
            } else {
                //there are surveillances for both orig and new listing activity
                //check all fields for equality
            }
        }
    }
}
