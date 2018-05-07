
## Version 15.11.0
_Date TBD_

### Minor Features
* Added error on upload for qms to be true but have empty standards and vice versa
* Restrict CMS IDs from using deleted Listings for /search or /create
* Added endpoint /certified_product/{certified_product} to return basic information about a certified product
* Added endpoint /certified_product/{certified_product}/cqm_results to return CQM results information about a certified product
* Added endpoint /certified_product/{certified_product}/certification_results to return certification results information about a certified product
* Added endpoint /certified_product/{chpl_product_number} to return basic information about a certified product
* Added endpoint /certified_product/{chpl_product_number}/cqm_results to return CQM results information about a certified product based on a CHPL Product Number
* Added endpoint /certified_product/{chpl_product_number}certificationtion_results to return certification results information about a certified product based on a CHPL Product Number
* Added endpoint /certified_product/{chpl_product_number}/certificationtion_results to return certification results information about a certified product based on a CHPL Product Number
* Added endpoint /certified_product/{chpl_product_number}/ics_relationships to return relationship tree information about a certified product based on a CHPL Product Number
* Added endpoint /activity/certified_product/{chpl_product_number} to return activity information about a certified product based on a CHPL Product Number

### Bugs Fixed
* Fixed bug with searching for Listings with lower case CHPL ID values
* Fix bug where uploaded Listings file with bad B1 cell isn't treated correctly

---
