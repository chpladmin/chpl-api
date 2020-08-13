package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
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

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductOwner;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.SplitProductsRequest;
import gov.healthit.chpl.domain.UpdateProductsRequest;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil.ChplProductNumberParts;
import gov.healthit.chpl.web.controller.results.ProductResults;
import gov.healthit.chpl.web.controller.results.SplitProductResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "products")
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductManager productManager;

    @Autowired
    private CertifiedProductManager cpManager;

    @Autowired
    private DeveloperManager developerManager;

    @Autowired
    private ChplProductNumberUtil chplProductNumberUtil;

    @Autowired
    private MessageSource messageSource;

    @ApiOperation(value = "List all products",
            notes = "Either list all products or optionally just all products belonging to a specific developer.")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ProductResults getAllProducts(@RequestParam(required = false) final Long developerId) {

        List<ProductDTO> productList = null;

        if (developerId != null && developerId > 0) {
            productList = productManager.getByDeveloper(developerId);
        } else {
            productList = productManager.getAll();
        }

        List<Product> products = new ArrayList<Product>();
        if (productList != null && productList.size() > 0) {
            for (ProductDTO dto : productList) {
                Product result = new Product(dto);
                products.add(result);
            }
        }

        ProductResults results = new ProductResults();
        results.setProducts(products);
        return results;
    }

    @ApiOperation(value = "Get information about a specific product.", notes = "")
    @RequestMapping(value = "/{productId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody Product getProductById(@PathVariable("productId") final Long productId)
            throws EntityRetrievalException {
        ProductDTO product = productManager.getById(productId);

        Product result = null;
        if (product != null) {
            result = new Product(product);
        }
        return result;
    }

    @ApiOperation(value = "Get all listings owned by the specified product.", notes = "")
    @RequestMapping(value = "/{productId}/listings", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertifiedProduct> getListingsForProduct(@PathVariable("productId") final Long productId)
            throws EntityRetrievalException {
        List<CertifiedProductDetailsDTO> listings = cpManager.getByProduct(productId);
        List<CertifiedProduct> results = new ArrayList<CertifiedProduct>();
        for (CertifiedProductDetailsDTO listing : listings) {
            results.add(new CertifiedProduct(listing));
        }
        return results;
    }

    @ApiOperation(value = "Update a product or merge products.",
            notes = "This method serves two purposes: to update a single product's information and to merge two "
                    + "products into one. A user of this service should pass in a single productId to update just "
                    + "that product.  If multiple product IDs are passed in, the service performs a merge meaning "
                    + "that a new product is created with all of the information provided and all of the versions "
                    + " previously assigned to the productIds specified are reassigned to the newly created product. "
                    + "The old products are then deleted. "
                    + "Security Restrictions: To merge: ROLE_ADMIN or ROLE_ONC.  To update: ROLE_ADMIN, ROLE_ONC or ROLE_ACB.")
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public ResponseEntity<Product> updateProduct(
            @RequestBody(required = true) final UpdateProductsRequest productInfo)
                    throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException,
                    JsonProcessingException, ValidationException {

        return update(productInfo);
    }

    private ResponseEntity<Product> update(final UpdateProductsRequest productInfo)
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException,
            JsonProcessingException, ValidationException {

        ProductDTO result = null;
        HttpHeaders responseHeaders = new HttpHeaders();

        if (productInfo.getProductIds() == null || productInfo.getProductIds().size() == 0) {
            throw new InvalidArgumentsException("At least one product id must be provided in the request.");
        }
        if (productInfo.getProductIds().size() > 1) {
            // if a product was sent in, we need to do a "merge" of the new product and old products create a new
            // product with the rest of the passed in information
            result = mergeProducts(productInfo);
            responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        } else if (productInfo.getProductIds().size() == 1) {
            if (productInfo.getNewDeveloperId() != null) {
                List<DuplicateChplProdNumber> duplicateChplProdNbrs =
                        getDuplicateChplProdNumbersCausedByDeveloperChange(
                                productInfo.getProductIds().get(0), productInfo.getNewDeveloperId());

                if (duplicateChplProdNbrs.size() != 0) {
                    throw new ValidationException(
                            getDuplicateChplProductNumberErrorMessages(duplicateChplProdNbrs), null);
                }
            }
            // build a new product dto that has all of the updated data
            ProductDTO toUpdate = new ProductDTO();
            toUpdate.setId(productInfo.getProductIds().get(0));
            toUpdate.setName(productInfo.getProduct().getName());
            toUpdate.setReportFileLocation(productInfo.getProduct().getReportFileLocation());
            if (productInfo.getProduct().getContact() != null) {
                ContactDTO contact = new ContactDTO();
                contact.setId(productInfo.getProduct().getContact().getContactId());
                contact.setFullName(productInfo.getProduct().getContact().getFullName());
                contact.setFriendlyName(productInfo.getProduct().getContact().getFriendlyName());
                contact.setTitle(productInfo.getProduct().getContact().getTitle());
                contact.setEmail(productInfo.getProduct().getContact().getEmail());
                contact.setPhoneNumber(productInfo.getProduct().getContact().getPhoneNumber());
                toUpdate.setContact(contact);
            }
            // update the developer if an id is supplied
            if (productInfo.getNewDeveloperId() != null) {
                toUpdate.getOwner().setId(productInfo.getNewDeveloperId());
            } else {
                toUpdate.getOwner().setId(productInfo.getProduct().getOwner().getDeveloperId());
            }
            // product could have updated ownership history
            if (productInfo.getProduct().getOwnerHistory() != null) {
                for (ProductOwner prevOwner : productInfo.getProduct().getOwnerHistory()) {
                    ProductOwnerDTO prevOwnerDTO = new ProductOwnerDTO();
                    prevOwnerDTO.setId(prevOwner.getId());
                    prevOwnerDTO.setProductId(toUpdate.getId());
                    DeveloperDTO dev = new DeveloperDTO();
                    dev.setId(prevOwner.getDeveloper().getDeveloperId());
                    prevOwnerDTO.setDeveloper(dev);
                    prevOwnerDTO.setTransferDate(prevOwner.getTransferDate());
                    toUpdate.getOwnerHistory().add(prevOwnerDTO);
                }
            }
            if (didOwnerHistoryChange(productInfo)) {
                result = productManager.updateProductOwnership(toUpdate);
            } else {
                result = productManager.update(toUpdate);
            }
            responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        }

        if (result == null) {
            throw new EntityCreationException("There was an error inserting or updating the product information.");
        }

        // get the updated product since all transactions should be complete by
        // this point
        ProductDTO updatedProduct = productManager.getById(result.getId());
        return new ResponseEntity<Product>(new Product(updatedProduct), responseHeaders, HttpStatus.OK);
    }

    private Boolean didOwnerHistoryChange(UpdateProductsRequest request) {
        try {
            ProductDTO origProduct = productManager.getById(request.getProduct().getProductId());
            if (origProduct != null) {
                //Convert the dto list to domain objects
                List<ProductOwner> origProductOwners = convertToProductOwnerDomainTypes(origProduct.getOwnerHistory());
                List<ProductOwner> newProductOwners = request.getProduct().getOwnerHistory();
                return !isProductOwnerListEqual(origProductOwners, newProductOwners);
            }
        } catch (EntityRetrievalException e) {
            return false;
        }
        return false;
    }

    private Boolean isProductOwnerListEqual(List<ProductOwner> owners1, List<ProductOwner> owners2) {
        for (ProductOwner po : owners1) {
            if (!owners2.contains(po)) {
                return false;
            }
        }
        for (ProductOwner po : owners2) {
            if (!owners1.contains(po)) {
                return false;
            }
        }

        return true;
    }

    private List<ProductOwner> convertToProductOwnerDomainTypes(List<ProductOwnerDTO> dtos) {
        List<ProductOwner> productOwners = new ArrayList<ProductOwner>();
        for (ProductOwnerDTO dto : dtos) {
            productOwners.add(new ProductOwner(dto));
        }
        return productOwners;
    }
    private Set<String> getDuplicateChplProductNumberErrorMessages(
            final List<DuplicateChplProdNumber> duplicateChplProdNumbers) {

        Set<String> messages = new HashSet<String>();

        for (DuplicateChplProdNumber dup : duplicateChplProdNumbers) {
            messages.add(String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("developer.update.dupChplProdNbrs"),
                    LocaleContextHolder.getLocale()),
                    dup.getOrigChplProductNumberA(),
                    dup.getOrigChplProductNumberB()));
        }
        return messages;
    }

    private List<DuplicateChplProdNumber> getDuplicateChplProdNumbersCausedByDeveloperChange(
            final Long productId, final Long newDeveloperId) throws EntityRetrievalException {

        List<DuplicateChplProdNumber> duplicateChplProductNumbers = new ArrayList<DuplicateChplProdNumber>();
        DeveloperDTO newDeveloper = developerManager.getById(newDeveloperId);

        //cpManager.getByProduct(productId)
        List<CertifiedProductDetailsDTO> newDeveloperCertifiedProducts =
                cpManager.getByDeveloperId(newDeveloperId);

        //Get the CPs, for the current product - the CHPL Prod Nbr will be changing
        List<CertifiedProductDetailsDTO> certifiedProducts = cpManager.getByProduct(productId);

        for (CertifiedProductDetailsDTO cpDTO : certifiedProducts) {
            String newChplProductNumber = "";
            if (cpDTO.getChplProductNumber().startsWith("CHP")) {
                newChplProductNumber = cpDTO.getChplProductNumber();
            } else {
                //Calculate the new CHPL Prod Nbr
                newChplProductNumber = chplProductNumberUtil.getChplProductNumber(
                        cpDTO.getYear(),
                        getTestingLabCode(cpDTO.getChplProductNumber()),
                        cpDTO.getCertificationBodyCode(),
                        newDeveloper.getDeveloperCode(),
                        cpDTO.getProductCode(),
                        cpDTO.getVersionCode(),
                        cpDTO.getIcsCode(),
                        cpDTO.getAdditionalSoftwareCode(),
                        cpDTO.getCertifiedDateCode());
            }

            //Does this CHPL prod number already exist for the new developer?
            List<CertifiedProductDetailsDTO> filteredResults =
                    filterByChplProductNumber(newDeveloperCertifiedProducts, newChplProductNumber);

            for (CertifiedProductDetailsDTO filterCp : filteredResults) {
                //Add it to the list of duplicates
                if (!cpDTO.getChplProductNumber().equals(filterCp.getChplProductNumber())) {
                    duplicateChplProductNumbers.add(new DuplicateChplProdNumber(
                            cpDTO.getChplProductNumber(), filterCp.getChplProductNumber(),
                            filterCp.getChplProductNumber()));
                }
            }
        }

        return duplicateChplProductNumbers;
    }

    private String getTestingLabCode(final String chplProductNumber) throws EntityRetrievalException {
        ChplProductNumberParts parts = chplProductNumberUtil.parseChplProductNumber(chplProductNumber);
        return parts.getAtlCode();
    }

    private List<CertifiedProductDetailsDTO> filterByChplProductNumber(
            final List<CertifiedProductDetailsDTO> certifiedProducts, final String chplProuctNumber) {
        List<CertifiedProductDetailsDTO> results = new ArrayList<CertifiedProductDetailsDTO>();

        for (CertifiedProductDetailsDTO cp : certifiedProducts) {
            if (cp.getChplProductNumber().equals(chplProuctNumber)) {
                results.add(cp);
            }
        }
        return results;
    }

    @ApiOperation(
            value = "Split a product - some versions stay with the existing product and some versions are moved "
                    + "to a new product.",
                    notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB")
    @RequestMapping(value = "/{productId}/split", method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public ResponseEntity<SplitProductResponse> splitProduct(@PathVariable("productId") final Long productId,
            @RequestBody(required = true) final SplitProductsRequest splitRequest)
                    throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException,
                    JsonProcessingException {

        if (splitRequest.getNewProductCode() != null) {
            splitRequest.setNewProductCode(splitRequest.getNewProductCode().trim());
        }
        if (StringUtils.isEmpty(splitRequest.getNewProductCode())) {
            throw new InvalidArgumentsException("A new product code is required.");
        }
        if (splitRequest.getNewProductName() != null) {
            splitRequest.setNewProductName(splitRequest.getNewProductName().trim());
        }
        if (StringUtils.isEmpty(splitRequest.getNewProductName())) {
            throw new InvalidArgumentsException("A new product name is required.");
        }
        if (splitRequest.getNewVersions() == null || splitRequest.getNewVersions().size() == 0) {
            throw new InvalidArgumentsException("At least one version to assign to the new product is required.");
        }
        if (splitRequest.getOldProduct() == null || splitRequest.getOldProduct().getProductId() == null) {
            throw new InvalidArgumentsException("An 'oldProduct' ID is required.");
        }
        if (splitRequest.getOldVersions() == null || splitRequest.getOldVersions().size() == 0) {
            throw new InvalidArgumentsException(
                    "At least one version must remain with the original product. No 'oldVersion's were found.");
        }
        if (productId.longValue() != splitRequest.getOldProduct().getProductId().longValue()) {
            throw new InvalidArgumentsException("The productId passed into the URL (" + productId
                    + ") does not match the product id specified in the request body ("
                    + splitRequest.getOldProduct().getProductId() + ").");
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        ProductDTO oldProduct = productManager.getById(splitRequest.getOldProduct().getProductId());
        ProductDTO newProduct = new ProductDTO();
        newProduct.setName(splitRequest.getNewProductName());
        newProduct.getOwner().setId(oldProduct.getOwner().getId());
        List<ProductVersionDTO> newProductVersions = new ArrayList<ProductVersionDTO>();
        for (ProductVersion requestVersion : splitRequest.getNewVersions()) {
            ProductVersionDTO newVersion = new ProductVersionDTO();
            newVersion.setId(requestVersion.getVersionId());
            newVersion.setVersion(requestVersion.getVersion());
            newProductVersions.add(newVersion);
        }
        ProductDTO splitProductNew = productManager.split(oldProduct, newProduct, splitRequest.getNewProductCode(),
                newProductVersions);
        responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        ProductDTO splitProductOld = productManager.getById(oldProduct.getId());
        SplitProductResponse response = new SplitProductResponse();
        response.setNewProduct(new Product(splitProductNew));
        response.setOldProduct(new Product(splitProductOld));

        // find out which CHPL product numbers would have changed (only
        // new-style ones)
        // and add them to the response header
        List<CertifiedProductDetailsDTO> possibleChangedChplIds = cpManager.getByProduct(splitProductNew.getId());
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
        return new ResponseEntity<SplitProductResponse>(response, responseHeaders, HttpStatus.OK);
    }

    private ProductDTO mergeProducts(final UpdateProductsRequest productInfo) throws JsonProcessingException, EntityRetrievalException, EntityCreationException {
        ProductDTO newProduct = new ProductDTO();
        newProduct.setName(productInfo.getProduct().getName());
        newProduct.setReportFileLocation(productInfo.getProduct().getReportFileLocation());
        if (productInfo.getNewDeveloperId() != null) {
            newProduct.getOwner().setId(productInfo.getNewDeveloperId());
        }
        // new product could be created with ownership history
        if (productInfo.getProduct().getOwnerHistory() != null) {
            for (ProductOwner prevOwner : productInfo.getProduct().getOwnerHistory()) {
                ProductOwnerDTO prevOwnerDTO = new ProductOwnerDTO();
                prevOwnerDTO.setId(prevOwner.getId());
                DeveloperDTO dev = new DeveloperDTO();
                dev.setId(prevOwner.getDeveloper().getDeveloperId());
                prevOwnerDTO.setDeveloper(dev);
                prevOwnerDTO.setTransferDate(prevOwner.getTransferDate());
                newProduct.getOwnerHistory().add(prevOwnerDTO);
            }
        }
        return productManager.merge(productInfo.getProductIds(), newProduct);
    }

    private class DuplicateChplProdNumber {
        private String origChplProductNumberA;
        private String origChplProductNumberB;
        private String newChplProductNumber;

        DuplicateChplProdNumber(final String origChplProductNumberA, final String origChplProductNumberB,
                final String newChplProductNumber) {
            this.origChplProductNumberA = origChplProductNumberA;
            this.origChplProductNumberB = origChplProductNumberB;
            this.newChplProductNumber = newChplProductNumber;
        }

        public String getOrigChplProductNumberA() {
            return origChplProductNumberA;
        }

        public void setOrigChplProductNumberA(final String origChplProductNumberA) {
            this.origChplProductNumberA = origChplProductNumberA;
        }

        public String getOrigChplProductNumberB() {
            return origChplProductNumberB;
        }

        public void setOrigChplProductNumberB(final String origChplProductNumberB) {
            this.origChplProductNumberB = origChplProductNumberB;
        }

        public String getNewChplProductNumber() {
            return newChplProductNumber;
        }

        public void setNewChplProductNumber(final String newChplProductNumber) {
            this.newChplProductNumber = newChplProductNumber;
        }

        @Override
        public String toString() {
            return String.format(
                    messageSource.getMessage(
                            new DefaultMessageSourceResolvable("developer.merge.dupChplProdNbrs.duplicate"),
                            LocaleContextHolder.getLocale()),
                    origChplProductNumberA,
                    origChplProductNumberB);
        }
    }
}
