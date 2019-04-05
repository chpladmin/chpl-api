
## Version 20.8.0
_Date TBD_

### New features
* Add /system-status API call which returns the combined server up/down status and the cache initliazing/ok status.
* Deprecated /status and /cache_status API calls. Will be removed in a future release.
* Add new API call /certified_products/pending/metadata for high-level information about pending listings. /certified_products/pending is now deprecated.
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

### Bug Fixes
* Upload surveillance job properly runs when there is an invalid CHPL Product Number in the upload file

---
