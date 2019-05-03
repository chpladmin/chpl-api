
## Version 20.10.0
_Date TBD_

### New Features
* Added endpoint /collections/decertified-developers to improve response time getting that data. This endpoint will eventually replace /decertifications/developers which has been deprecated.
* Updated security for edit/split/merge of developers, products, and versions
* Updated product and version activity metadata to parse activity for merges and splits; better parsing of product and developer names if they have been deleted.
* Added endpoints for ACB and ATL activity metadata.
  * /activity/metadata/acbs
  * /activity/metadata/acb/{acbId}
  * /activity/metadata/atls
  * /activity/metadata/atl/{atlId}
* Deprecated existing endpoints for ACB and ATL activity
  * /activity/acbs
  * /activity/acb/{acbId}
  * /activity/atls
  * /activity/atl/{atlId}

---
