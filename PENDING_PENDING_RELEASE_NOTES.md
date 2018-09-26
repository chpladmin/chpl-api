
## Version 16.5.0
_Date TBD_

### New Features
* Move surveillance download file generation into quartz job to allow for better job management.
* Move SED G3 download file generation into quartz job for allow for better job management.
* Added scheduled job to send warning to users who have not used thier API key in the last X days
* Added scheduled job to delete api keys that have not been used in X days
* Added LastUsedDate and DeleteWarningSentDate to the ./key endpoint return value(s)
* Add password strength checker when anyone:
  * creates a new User
  * updates a password
* Add Developer Status to search results view
* Catch any unexpected error that occurs during a listing upload. Allow the error to be emailed to CHPL team if desired.

---
