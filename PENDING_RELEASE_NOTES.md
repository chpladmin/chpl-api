
## Version 20.6.0
_Date TBD_

### New features
* Add error message for unparseable certification date on Listing upload
* Allow certain users to impersonate certain other ones
* Add new endpoints for activity metadata for lisings, developers, products, and versions. Endpoints are:
  * /activity/metadata/listings
  * /activity/metadata/listings/{id}
  * /activity/metadata/developers
  * /activity/metadata/developers/{id}
  * /activity/metadata/products
  * /activity/metadata/products/{id}
  * /activity/metadata/versions
  * /activity/metadata/versions/{id}
* Add new activity details endpoint to get the detailed activity json 
  * /activity/details/{id}

### Refactoring changes
* Removed unused "compliance terms accepted" references

### Bug Fixes
* Fixed error on banned developer page when banned developer has listings without meaningful use counts.
* Use correct ROLE for surveillance authority

---
