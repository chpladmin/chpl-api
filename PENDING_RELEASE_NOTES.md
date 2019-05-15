

## Version 20.11.0
_Date TBD_

### New Features
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
* Add endpoints to provide ability for users to save filters for admin reports

### Bugs Fixed
* Correctly handle scheduled job update when an ACB is renamed.

---
