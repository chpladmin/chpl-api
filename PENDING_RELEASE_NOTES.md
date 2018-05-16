
## Version 15.12.0
_Date TBD_

### Minor Features
* Added endpoint /certified_product/{chpl_product_number} to return basic information about a certified product
* Added endpoint /certified_product/{chpl_product_number}/cqm_results to return CQM results information about a certified product based on a CHPL Product Number
* Added endpoint /certified_product/{chpl_product_number}certificationtion_results to return certification results information about a certified product based on a CHPL Product Number
* Added endpoint /certified_product/{chpl_product_number}/certificationtion_results to return certification results information about a certified product based on a CHPL Product Number
* Added endpoint /certified_product/{chpl_product_number}/ics_relationships to return relationship tree information about a certified product based on a CHPL Product Number
* Added endpoint /activity/certified_product/{chpl_product_number} to return activity information about a certified product based on a CHPL Product Number
* New chart added: Criterion / Product statistics
  * Update chart data generation application
  * Added API endpoint to retrieve chart data

### Bugs Fixed
* Fixed Summary Statistics Report missing ISCA count under the Total # of Unique Products with Active 2015 Listings section
* Respect configured questionable activity window to allow a user to make edits that do not get logged as questionable activity for a short amount of time after a listing's certification date.

---
