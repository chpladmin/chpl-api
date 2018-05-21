
## Version 15.12.0
_Date TBD_

### Minor Features
* Add endpoint /certified_product/{chpl_product_number} to return basic information about a certified product
* Add endpoint /certified_product/{chpl_product_number}/cqm_results to return CQM results information about a certified product based on a CHPL Product Number
* Add endpoint /certified_product/{chpl_product_number}certificationtion_results to return certification results information about a certified product based on a CHPL Product Number
* Add endpoint /certified_product/{chpl_product_number}/certificationtion_results to return certification results information about a certified product based on a CHPL Product Number
* Add endpoint /certified_product/{chpl_product_number}/ics_relationships to return relationship tree information about a certified product based on a CHPL Product Number
* Add endpoint /activity/certified_product/{chpl_product_number} to return activity information about a certified product based on a CHPL Product Number
* Add new chart: Criterion / Product statistics
  * Update chart data generation application
  * Add API endpoint to retrieve chart data

### Bugs Fixed
* Fix Summary Statistics Report missing ISCA count under the Total # of Unique Products with Active 2015 Listings section
* Respect configured questionable activity window to allow a user to make edits that do not get logged as questionable activity for a short amount of time after a listing's certification date.
* Fix bug restricting add/edit of Versions of Test Data

---
