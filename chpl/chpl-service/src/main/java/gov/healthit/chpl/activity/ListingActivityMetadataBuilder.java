package gov.healthit.chpl.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ListingActivityMetadata;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.ActivityDTO;

@Component("listingActivityMetadataBuilder")
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
        if (listing.getCertifyingBody() != null
                && listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY) != null) {
            listingMetadata.setAcbName(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString());
        }
        //there is at least one activity record that has a null certification date field
        //due to a bug in the system at the time of the activity
        listingMetadata.setCertificationDate(listing.getCertificationDate());
        if (listing.getDeveloper() != null) {
            listingMetadata.setDeveloperName(listing.getDeveloper().getName());
        }
        if (listing.getCertificationEdition() != null
                && listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY) != null) {
            listingMetadata.setEdition(listing.getCertificationEdition()
                    .get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString());
        }
        if (listing.getCuresUpdate() != null) {
            listingMetadata.setCuresUpdate(listing.getCuresUpdate());
        }
        if (listing.getProduct() != null) {
            listingMetadata.setProductName(listing.getProduct().getName());
        }
    }

    private void categorizeActivity(final ListingActivityMetadata listingMetadata,
            final CertifiedProductSearchDetails origListing, final CertifiedProductSearchDetails newListing) {
        listingMetadata.getCategories().add(ActivityCategory.LISTING);
        if (origListing == null && newListing != null) {
            listingMetadata.getCategories().add(ActivityCategory.LISTING_UPLOAD);
        } else if (origListing != null && newListing != null) {
            //status change?
            if (origListing.getCertificationStatus() != null && newListing.getCertificationStatus() != null
                    && origListing.getCertificationStatus().getId() != newListing.getCertificationStatus().getId()) {
                listingMetadata.getCategories().add(ActivityCategory.LISTING_STATUS_CHANGE);
            } else if (origListing.getCurrentStatus() != null && newListing.getCurrentStatus() != null
                    && origListing.getCurrentStatus().getStatus() != null
                    && newListing.getCurrentStatus().getStatus() != null
                    && !origListing.getCurrentStatus().getStatus().getName()
                    .equals(newListing.getCurrentStatus().getStatus().getName())) {
                listingMetadata.getCategories().add(ActivityCategory.LISTING_STATUS_CHANGE);
            }
            //surveillance change?
            //check for surveillance added or removed
            if ((origListing.getSurveillance() != null && newListing.getSurveillance() == null)
                    || (origListing.getSurveillance() == null && newListing.getSurveillance() != null)) {
                listingMetadata.getCategories().add(ActivityCategory.SURVEILLANCE);
            } else if (origListing.getSurveillance() != null && newListing.getSurveillance() != null) {
                if (origListing.getSurveillance().size() != newListing.getSurveillance().size()) {
                    listingMetadata.getCategories().add(ActivityCategory.SURVEILLANCE);
                } else {
                    //there are the same amount of surveillances for both orig and
                    //new listing activity;
                    //check for new surveillance, deleted surveillance, or any updates

                    //look for surveillance added
                    for (Surveillance newSurv : newListing.getSurveillance()) {
                        boolean foundInOrigListing = false;
                        for (Surveillance origSurv : origListing.getSurveillance()) {
                            if (origSurv.getId().longValue() == newSurv.getId().longValue()) {
                                foundInOrigListing = true;
                            }
                        }
                        if (!foundInOrigListing) {
                            //surv is in the new listing but not the original one = was added
                            listingMetadata.getCategories().add(ActivityCategory.SURVEILLANCE);
                        }
                    }

                    //if there's a surveillance change already detected we don't need to look any farther
                    //if not keep looking for one - look for surveillance deleted
                    if (!listingMetadata.getCategories().contains(ActivityCategory.SURVEILLANCE)) {
                        for (Surveillance origSurv : origListing.getSurveillance()) {
                            boolean foundInNewListing = false;
                            for (Surveillance newSurv : newListing.getSurveillance()) {
                                if (origSurv.getId().longValue() == newSurv.getId().longValue()) {
                                    foundInNewListing = true;
                                }
                            }
                            if (!foundInNewListing) {
                                //surv is in the original listing but not the new one = was deleted
                                listingMetadata.getCategories().add(ActivityCategory.SURVEILLANCE);
                            }
                        }
                    }

                    //if there's a surveillance change already detected we don't need to look any farther
                    //if not keep looking for one - look for surveillance updated
                    if (!listingMetadata.getCategories().contains(ActivityCategory.SURVEILLANCE)) {
                        for (Surveillance origSurv : origListing.getSurveillance()) {
                            for (Surveillance newSurv : newListing.getSurveillance()) {
                                if (origSurv.getId().longValue() == newSurv.getId().longValue()
                                        && !origSurv.matches(newSurv)) {
                                    listingMetadata.getCategories().add(ActivityCategory.SURVEILLANCE);
                                    //if we add a surveillance category there's no need to keep looking
                                    //for more differences.
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
