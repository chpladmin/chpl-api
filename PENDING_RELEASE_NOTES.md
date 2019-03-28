
## Version 20.7.0
_Date TBD_

### New features
* Add /system-status API call which returns the combined server up/down status and the cache initliazing/ok status.
* Deprecated /status and /cache_status API calls. Will be removed in a future release.
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

---
