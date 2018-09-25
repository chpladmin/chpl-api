
## Version 17.0.0
_Date TBD_

### Major Change
* When retrieving Pending Surveillances, the listing details will no longer be populated

### New Features
* Added scheduled job to send warning to users who have not used thier API key in the last X days
* Added scheduled job to delete api keys that have not been used in X days
* Added LastUsedDate and DeleteWarningSentDate to the ./key endpoint return value(s)
* Add password strength checker when anyone:
  * creates a new User
  * updates a password

---
