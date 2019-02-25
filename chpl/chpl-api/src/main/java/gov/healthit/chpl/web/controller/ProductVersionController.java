package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.UpdateVersionsRequest;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
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

        return update(versionInfo);
    }

    private ResponseEntity<ProductVersion> update(final UpdateVersionsRequest versionInfo)
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

}
