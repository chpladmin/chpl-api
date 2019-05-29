
## Version 21.1.0
_Date TBD_

### New Features
* Add FF4j framework (feature flags)
* Add endpoints to provide ability for users to save filters for admin reports
* Updated product and version activity metadata to parse activity for merges and splits; better parsing of product and developer names if they have been deleted.
* Respond with a 401 error when the user's token has expired

### Bugs Fixed
* Correctly handle scheduled job update when an ACB is renamed.
* Send 401 error back to consumer if JWT token is invalid

---
