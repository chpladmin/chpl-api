package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.SplitVersionsRequest;
import gov.healthit.chpl.domain.UpdateVersionsRequest;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.web.controller.results.SplitVersionResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "versions")
@RestController
@RequestMapping("/versions")
public class ProductVersionController {

    @Autowired
    private ProductVersionManager pvManager;
    @Autowired
    private ProductManager productManager;
    @Autowired
    private CertifiedProductManager cpManager;
    @Autowired
    private ChplProductNumberUtil chplProductNumberUtil;
    @Autowired
    private FF4j ff4j;

    @ApiOperation(value = "List all versions for a specific product.",
            notes = "List all versions associated with a specific product.")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ProductVersion> getVersionsByProduct(@RequestParam(required = true) final Long productId)
            throws EntityRetrievalException {
        //make sure the product exists
        productManager.getById(productId);

        //get the versions
        List<ProductVersionDTO> versionList = null;
        if (productId != null && productId > 0) {
            versionList = pvManager.getByProduct(productId);
        } else {
            versionList = pvManager.getAll();
        }

        List<ProductVersion> versions = new ArrayList<ProductVersion>();
        if (versionList != null && versionList.size() > 0) {
            for (ProductVersionDTO dto : versionList) {
                ProductVersion result = new ProductVersion(dto);
                versions.add(result);
            }
        }
        return versions;
    }

    @ApiOperation(value = "Get information about a specific version.", notes = "")
    @RequestMapping(value = "/{versionId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ProductVersion getProductVersionById(@PathVariable("versionId") final Long versionId)
            throws EntityRetrievalException {
        ProductVersionDTO version = pvManager.getById(versionId);

        ProductVersion result = null;
        if (version != null) {
            result = new ProductVersion(version);
        }
        return result;
    }

    @ApiOperation(value = "Update a version or merge versions.",
            notes = "This method serves two purposes: to update a single version's information and to merge two "
                    + "versions into one.  A user of this service should pass in a single versionId to update just "
                    + "that version.  If multiple version IDs are passed in, the service performs a merge meaning "
                    + "that a new version is created with all of the information provided and all of the certified "
                    + "products previously assigned to the old versionIds are reassigned to the newly created version."
                    + "  The old versions are then deleted. "
                    + "Security Restrictions: Must have ROLE_ADMIN to merge or ROLE_ACB and have administrative "
                    + "authority on the specified ACB to do all actions except merge.")
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public ResponseEntity<ProductVersion> updateVersion(
            @RequestBody(required = true) final UpdateVersionsRequest versionInfo)
                    throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException,
                    JsonProcessingException {

        ProductVersionDTO result = null;
        HttpHeaders responseHeaders = new HttpHeaders();

        if (versionInfo.getVersionIds() == null || versionInfo.getVersionIds().size() == 0) {
            throw new InvalidArgumentsException("At least one version id must be provided in the request.");
        }

        if (versionInfo.getVersion() == null && versionInfo.getNewProductId() != null) {
            // no new version is specified, so we just need to update the
            // product id
            for (Long versionId : versionInfo.getVersionIds()) {
                ProductVersionDTO toUpdate = pvManager.getById(versionId);
                if (versionInfo.getNewProductId() != null) {
                    toUpdate.setProductId(versionInfo.getNewProductId());
                }
                result = pvManager.update(toUpdate);
                responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
            }
        } else {
            if (versionInfo.getVersionIds().size() > 1) {
                // if a version was send in, we need to do a "merge" of the new
                // version and old versions
                // create a new version with the rest of the passed in
                // information
                if (versionInfo.getNewProductId() == null) {
                    throw new InvalidArgumentsException("A product ID must be specified.");
                }

                ProductVersionDTO newVersion = new ProductVersionDTO();
                newVersion.setVersion(versionInfo.getVersion().getVersion());
                newVersion.setProductId(versionInfo.getNewProductId());
                result = pvManager.merge(versionInfo.getVersionIds(), newVersion);
                responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
            } else if (versionInfo.getVersionIds().size() == 1) {
                // update the given version id with new data
                ProductVersionDTO toUpdate = new ProductVersionDTO();
                toUpdate.setId(versionInfo.getVersionIds().get(0));
                toUpdate.setVersion(versionInfo.getVersion().getVersion());
                if (versionInfo.getNewProductId() != null) {
                    toUpdate.setProductId(versionInfo.getNewProductId());
                }
                result = pvManager.update(toUpdate);
                responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
            }
        }

        if (result == null) {
            throw new EntityCreationException("There was an error inserting or updating the version information.");
        }
        return new ResponseEntity<ProductVersion>(new ProductVersion(result), responseHeaders, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Split a version - some listings stay with the existing version and some listings are moved "
                    + "to a new version.",
                    notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB")
    @RequestMapping(value = "/{versionId}/split", method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public ResponseEntity<SplitVersionResponse> splitVersion(@PathVariable("versionId") final Long versionId,
            @RequestBody(required = true) final SplitVersionsRequest splitRequest)
                    throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException,
                    JsonProcessingException {
        if (!ff4j.check(FeatureList.BETTER_SPLIT)) {
            throw new NotImplementedException();
        }

        if (splitRequest.getNewVersionCode() != null) {
            splitRequest.setNewVersionCode(splitRequest.getNewVersionCode().trim());
        }
        if (StringUtils.isEmpty(splitRequest.getNewVersionCode())) {
            throw new InvalidArgumentsException("A new version code is required.");
        }
        if (splitRequest.getNewVersionVersion() != null) {
            splitRequest.setNewVersionVersion(splitRequest.getNewVersionVersion().trim());
        }
        if (StringUtils.isEmpty(splitRequest.getNewVersionVersion())) {
            throw new InvalidArgumentsException("A new product name is required.");
        }
        if (splitRequest.getNewListings() == null || splitRequest.getNewListings().size() == 0) {
            throw new InvalidArgumentsException("At least one listing to assign to the new version is required.");
        }
        if (splitRequest.getOldVersion() == null || splitRequest.getOldVersion().getVersionId() == null) {
            throw new InvalidArgumentsException("An 'oldVersion' ID is required.");
        }
        if (splitRequest.getOldListings() == null || splitRequest.getOldListings().size() == 0) {
            throw new InvalidArgumentsException(
                    "At least one listing must remain with the original version. No 'oldListings's were found.");
        }
        if (versionId.longValue() != splitRequest.getOldVersion().getVersionId().longValue()) {
            throw new InvalidArgumentsException("The versionId passed into the URL (" + versionId
                    + ") does not match the version id specified in the request body ("
                    + splitRequest.getOldVersion().getVersionId() + ").");
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        ProductVersionDTO oldVersion = pvManager.getById(splitRequest.getOldVersion().getVersionId());
        ProductVersionDTO newVersion = new ProductVersionDTO();
        newVersion.setVersion(splitRequest.getNewVersionVersion());
        newVersion.setProductId(oldVersion.getProductId());
        newVersion.setDeveloperId(oldVersion.getDeveloperId());
        List<CertifiedProductDTO> newVersionListings = new ArrayList<CertifiedProductDTO>();
        for (CertifiedProduct requestNewVersionListing : splitRequest.getNewListings()) {
            CertifiedProductDTO newVersionListing = new CertifiedProductDTO();
            newVersionListing.setId(requestNewVersionListing.getId());
            newVersionListing.setVersionCode(splitRequest.getNewVersionCode());
        }

        ProductVersionDTO newVersionFromSplit = pvManager.split(oldVersion, newVersion, splitRequest.getNewVersionCode(),
                newVersionListings);
        responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        SplitVersionResponse response = new SplitVersionResponse();
        response.setNewVersion(new ProductVersion(newVersionFromSplit));
        response.setOldVersion(new ProductVersion(oldVersion));

        // find out which CHPL product numbers would have changed (only
        // new-style ones)
        // and add them to the response header
        List<CertifiedProductDetailsDTO> possibleChangedChplIds = cpManager.getByVersion(newVersionFromSplit.getId());
        if (possibleChangedChplIds != null && possibleChangedChplIds.size() > 0) {
            StringBuffer buf = new StringBuffer();
            for (CertifiedProductDetailsDTO possibleChanged : possibleChangedChplIds) {
                if (!chplProductNumberUtil.isLegacy(possibleChanged.getChplProductNumber())) {
                    if (buf.length() > 0) {
                        buf.append(",");
                    }
                    buf.append(possibleChanged.getChplProductNumber());
                }
            }
            responseHeaders.set("CHPL-Id-Changed", buf.toString());
        }
        return new ResponseEntity<SplitVersionResponse>(response, responseHeaders, HttpStatus.OK);
    }
}
