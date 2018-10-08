
## Version 17.0.0
_Date TBD_

### Major Change
* When retrieving Pending Surveillances, the listing details will no longer be populated

### New Features
* Move surveillance download file generation into quartz job to allow for better job management.
* Move SED G3 download file generation into quartz job for allow for better job management.
* Added scheduled job to send warning to users who have not used thier API key in the last X days
* Added scheduled job to delete api keys that have not been used in X days
* Added LastUsedDate and DeleteWarningSentDate to the ./key endpoint return value(s)
* Add password strength checker implementing [a java fork](https://github.com/nulab/zxcvbn4j) of [zxcvbn](https://github.com/dropbox/zxcvbn) when anyone:
  * creates a new User
  * updates a password
* Add Developer Status to search results view
* Catch any unexpected error that occurs during a listing upload. Allow the error to be emailed to CHPL team if desired.

### Bugs Fixed
* Invalid test functionality names are removed and an error is given to the user for 2014 and 2015 uploads.
* Fix questionable activity report end of the month date rollover.

---
